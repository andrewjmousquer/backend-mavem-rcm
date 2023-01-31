package com.portal.utils;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import com.portal.enums.LeadEvents;
import com.portal.enums.LeadState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.exceptions.StateWorkflowException;
import com.portal.model.Lead;
import com.portal.model.LeadWorkflow;
import com.portal.service.ILeadService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LeadStateBuilder {

	public static final String LEAD_INSTANCE = "LEAD_INSTANCE";
	
	@Autowired
	ILeadService leadService;
	
	public StateMachine<LeadState, LeadEvents> buildNewStateMachine() throws Exception {
		return this.buildNewStateMachine(null);
	}
	
	public StateMachine<LeadState, LeadEvents> buildNewStateMachine( String id ) throws Exception {
		StateMachineBuilder.Builder<LeadState, LeadEvents> builder = StateMachineBuilder.builder();
		
		if( id == null ) {
			id =  UUID.randomUUID().toString();
		}
		
		builder.configureConfiguration()
						.withConfiguration()
						.machineId( id )
						.autoStartup(true)
						.listener( listener( id ) );
		
		builder.configureStates()
					.withStates()
						.initial(LeadState.OPENED)
						.states(EnumSet.allOf(LeadState.class))
						.end(LeadState.CANCELED);
		
		builder.configureTransitions()
					.withExternal()
						.source(LeadState.OPENED)
						.target(LeadState.CONTACTED)
						.event(LeadEvents.CONTACT)
						.guard( validatorGuard( LeadState.CONTACTED ) )
					.and()
					.withExternal()
						.source(LeadState.OPENED)
						.target(LeadState.CANCELED)
						.event(LeadEvents.CANCEL)
						.guard( validatorGuard( LeadState.CANCELED ) )
					.and()
					.withExternal()
						.source(LeadState.CONTACTED)
						.target(LeadState.CONVERTED)
						.event(LeadEvents.CONVERT)
						.guard( validatorGuard( LeadState.CONVERTED ) )
					.and()
					.withExternal()
						.source(LeadState.CONTACTED)
						.target(LeadState.UNCONVERTED)
						.event(LeadEvents.NOT_CONVERT)
						.guard( validatorGuard( LeadState.UNCONVERTED ) )
					.and()
					.withExternal()
						.source(LeadState.CONTACTED)
						.target(LeadState.CANCELED)
						.event(LeadEvents.CANCEL)
						.guard( validatorGuard( LeadState.CANCELED ) )
					.and()
					.withExternal()
						.source(LeadState.CONVERTED)
						.target(LeadState.CANCELED)
						.event(LeadEvents.CANCEL)
						.guard( validatorGuard( LeadState.CANCELED ) )
					.and()
					.withExternal()
						.source(LeadState.CONVERTED)
						.target(LeadState.UNCONVERTED)
						.event(LeadEvents.NOT_CONVERT)
						.guard( validatorGuard( LeadState.UNCONVERTED ) )
					.and()
					.withExternal()
						.source(LeadState.UNCONVERTED)
						.target(LeadState.CANCELED)
						.event(LeadEvents.CANCEL)
						.guard( validatorGuard( LeadState.CANCELED ) )
					.and()
					.withExternal()
						.source(LeadState.UNCONVERTED)
						.target(LeadState.CONVERTED)
						.event(LeadEvents.CONVERT)
						.guard( validatorGuard( LeadState.CONVERTED ) )
					.and()
					.withInternal()
						.source(LeadState.OPENED)
						.event(LeadEvents.OPEN)
						.guard( validatorGuard( LeadState.OPENED ) )
					.and()
					.withInternal()
						.source(LeadState.CONTACTED)
						.event(LeadEvents.CONTACT)
						.guard( validatorGuard( LeadState.CONTACTED ) )
					.and()
					.withInternal()
						.source(LeadState.CANCELED)
						.event(LeadEvents.CANCEL)
						.guard( validatorGuard( LeadState.CANCELED ) )
					.and()
					.withInternal()
						.source(LeadState.CONVERTED)
						.event(LeadEvents.CONVERT)
						.guard( validatorGuard( LeadState.CONVERTED ) )
					.and()
					.withInternal()
						.source(LeadState.UNCONVERTED)
						.event(LeadEvents.NOT_CONVERT)
						.guard( validatorGuard( LeadState.UNCONVERTED ) )
					;
		
		return builder.build();
	}
	
	public Guard<LeadState, LeadEvents> validatorGuard( LeadState target ) {
		return  ctx -> {
			try {
				
				Lead lead = (Lead) ctx.getMessageHeader( LEAD_INSTANCE );
				
				switch ( target ) {
					case OPENED:
						leadService.validateOpenedState(lead);
						break;
					case CANCELED:
						leadService.validateCanceledState(lead);
						break;
					case CONTACTED:
						leadService.validateContactedState(lead);
						break;
					case CONVERTED:
						leadService.validateConvertedState(lead);
						break;
					case UNCONVERTED:
						leadService.validateUnConvertedState(lead);
						break;
					default:
						throw new BusException( "O status " + target + " é inválido ou não existe." );
				}
				
				return true;
				
			} catch (BusException | AppException e) {
				StateWorkflowException ex = new StateWorkflowException( e );
				ctx.getStateMachine().setStateMachineError( ex );
				ctx.getExtendedState().getVariables().put( "stateException", ex );
			}
			
			return false;
		};
	}

	public StateMachineListener<LeadState, LeadEvents> listener( String id ) {
		return new StateMachineListenerAdapter<LeadState, LeadEvents>() {
			@Override
			public void stateChanged(State<LeadState, LeadEvents> from, State<LeadState, LeadEvents> to) {
				log.debug( "LeadState-stateChange ID: {} - From {} to {}", id, (from == null ? "N/A" : from.getId() ), (to == null ? "N/A" : to.getId() ) );
			}
			
			@Override
			public void stateMachineError(StateMachine<LeadState, LeadEvents> stateMachine, Exception exception) {
				log.warn( "LeadState-stateMachineError ID: {} SM: {}", id, stateMachine, exception );
			}
			
			@SuppressWarnings("rawtypes")
			@Override
		    public void eventNotAccepted( Message event ) {
				log.warn( "LeadState-eventNotAccepted ID: {} Event: {}", id, event );
		    }
		};
	}

	/**
	 * Recupera a máquina de estado com base no estado passado no argumento
	 *  
	 * @param id ID que ser dado a máquina de status	
	 * @param state estado a ser carregado como de partida da máquina.	
	 * @return instância da máquina de estado.
	 */
	public StateMachine<LeadState, LeadEvents> recoveryStateMachine( String id, LeadState state ) throws BusException, AppException {
		if( state == null) {
			throw new AppException( "Não é possível carregar a máquina com o estado inválido." ); 
		}
		
		try {
			StateMachine<LeadState, LeadEvents> sm = this.buildNewStateMachine( id );
			sm.stop();
			
			sm.getStateMachineAccessor()
					.doWithAllRegions( sma -> {
						sma.resetStateMachine( new DefaultStateMachineContext<>( state , null, null, null) );
					} );
			
			sm.start();
			
			return sm;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar a máquina de estado do lead.", e );
			throw new AppException( e );
		}
	}

	/**
	 * Valida se a transição entre 2 status é válida.
	 * 
	 * @param sm			instância da máquina de status
	 * @param source		status de origem para a validação
	 * @param target		status de destino para a validação
	 * @return				true se a troca entre os status é válida, false caso não seja válido
	 * @throws BusException
	 * @throws AppException
	 */
	public static boolean isStateTransitionValid( StateMachine<LeadState, LeadEvents> sm, LeadState source, LeadState target ) throws BusException, AppException {
		
		if( sm == null ) {
			throw new AppException( "Máquina de estado inválida." );
		}
		
		if( source == null || target == null ) {
			throw new AppException( "Status de origiem e/ou destino inválidos." );
		}
		
		
		if( sm.getTransitions() != null ) {
			
			Map<LeadState, List<LeadState>> map = sm.getTransitions().stream()
														.collect( 
															Collectors.groupingBy( 
																t -> t.getSource().getId(),
																Collectors.mapping(
																	t -> t.getTarget().getId(), Collectors.toList()
																)
															)
														);
			
			return map.get( source ).stream().anyMatch( i -> i.equals( target ) );
			
		}
		
		return false;
	}

	/**
	 * Com base na máquina de estado passada montamos a lista de ORIGEM / DESTINO e AÇÃO que é possível ser executado.
	 * 
	 * @param sm			 máquina de estado do Lead
	 * @return				 lista com as combinações de ORIGEM, DESTINO e AÇÂO
	 * @throws BusException
	 * @throws AppException
	 */
	public static List<LeadWorkflow> getStateWorkflowTree( StateMachine<LeadState, LeadEvents> sm ) throws BusException, AppException {
		
		if( sm == null ) {
			throw new AppException( "Máquina de estado inválida." );
		}
		
		List<LeadWorkflow> tree = new ArrayList<>();
		
		if( sm.getTransitions() != null ) {
			
			sm.getTransitions().forEach( t -> {
				tree.add(
						LeadWorkflow.builder()
										.from( t.getSource().getId() )
										.to( t.getTarget().getId() )
										.action( t.getTrigger().getEvent() )
										.build()
									);
			});
		}
		
		return tree;
	}
}
