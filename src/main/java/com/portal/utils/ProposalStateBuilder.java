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

import com.portal.enums.ProposalEvents;
import com.portal.enums.ProposalState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.exceptions.StateWorkflowException;
import com.portal.model.Proposal;
import com.portal.model.ProposalWorkflow;
import com.portal.service.IProposalService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProposalStateBuilder {

	public static final String PROPOSAL_INSTANCE = "PROPOSAL_INSTANCE";
	
	@Autowired
	IProposalService proposalService;
	
	public StateMachine<ProposalState, ProposalEvents> buildNewStateMachine() throws Exception {
		return this.buildNewStateMachine(null);
	}
	
	public StateMachine<ProposalState, ProposalEvents> buildNewStateMachine( String id ) throws Exception {
		StateMachineBuilder.Builder<ProposalState, ProposalEvents> builder = StateMachineBuilder.builder();
		
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
						.initial(ProposalState.IN_PROGRESS)
						.states(EnumSet.allOf(ProposalState.class))
						.end(ProposalState.CANCELED);
		
		builder.configureTransitions()
					.withExternal()
						.source(ProposalState.IN_PROGRESS)
						.target(ProposalState.IN_COMMERCIAL_APPROVAL)
						.event(ProposalEvents.REQUEST_COMMECIAL_APPROVAL)
						.guard( validatorGuard( ProposalState.IN_COMMERCIAL_APPROVAL ) )
					.and()
					.withExternal()
						.source(ProposalState.IN_PROGRESS)
						.target(ProposalState.ON_CUSTOMER_APPROVAL)
						.event(ProposalEvents.SEND_TO_CUSTOMER)
						.guard( validatorGuard( ProposalState.ON_CUSTOMER_APPROVAL ) )
					.and()
					.withExternal()
						.source(ProposalState.IN_COMMERCIAL_APPROVAL)
						.target(ProposalState.COMMERCIAL_DISAPPROVED)
						.event(ProposalEvents.DISAPPROVED_COMMERCIAL)
						.guard( validatorGuard( ProposalState.COMMERCIAL_DISAPPROVED ) )
					.and()
					.withExternal()
						.source(ProposalState.IN_COMMERCIAL_APPROVAL)
						.target(ProposalState.COMMERCIAL_APPROVED)
						.event(ProposalEvents.APPROVED_COMMERCIAL)
						.guard( validatorGuard( ProposalState.COMMERCIAL_APPROVED ) )
					.and()
					.withExternal()
						.source(ProposalState.COMMERCIAL_DISAPPROVED)
						.target(ProposalState.IN_COMMERCIAL_APPROVAL)
						.event(ProposalEvents.REQUEST_COMMECIAL_APPROVAL)
						.guard( validatorGuard( ProposalState.IN_COMMERCIAL_APPROVAL ) )
					.and()
					.withExternal()
						.source(ProposalState.COMMERCIAL_DISAPPROVED)
						.target(ProposalState.ON_CUSTOMER_APPROVAL)
						.event(ProposalEvents.SEND_TO_CUSTOMER)
						.guard( validatorGuard( ProposalState.ON_CUSTOMER_APPROVAL ) )
					.and()
					.withExternal()
						.source(ProposalState.COMMERCIAL_APPROVED)
						.target(ProposalState.IN_PROGRESS)
						.event(ProposalEvents.RENEGOTIATION)
						.guard( validatorGuard( ProposalState.IN_PROGRESS ) )
					.and()
					.withExternal()
						.source(ProposalState.COMMERCIAL_APPROVED)
						.target(ProposalState.ON_CUSTOMER_APPROVAL)
						.event(ProposalEvents.SEND_TO_CUSTOMER)
						.guard( validatorGuard( ProposalState.ON_CUSTOMER_APPROVAL ) )
					.and()
					.withExternal()
						.source(ProposalState.ON_CUSTOMER_APPROVAL)
						.target(ProposalState.FINISHED_WITH_SALE)
						.event(ProposalEvents.CUSTOMER_APPROVED)
						.guard( validatorGuard( ProposalState.FINISHED_WITH_SALE ) )
					.and()
					.withExternal()
						.source(ProposalState.ON_CUSTOMER_APPROVAL)
						.target(ProposalState.IN_PROGRESS)
						.event(ProposalEvents.RENEGOTIATION)
						.guard( validatorGuard( ProposalState.IN_PROGRESS ) )
					.and()
					.withExternal()
						.source(ProposalState.ON_CUSTOMER_APPROVAL)
						.target(ProposalState.FINISHED_WITHOUT_SALE)
						.event(ProposalEvents.CUSTOMER_DISAPPROVED)
						.guard( validatorGuard( ProposalState.FINISHED_WITHOUT_SALE ) )
					;
		
		return builder.build();
	}
	
	private Guard<ProposalState, ProposalEvents> validatorGuard( ProposalState target ) {
		return  ctx -> {
			try {
				
				Proposal proposal = (Proposal) ctx.getMessageHeader( PROPOSAL_INSTANCE );
				
				switch ( target ) {
					case IN_PROGRESS:
						this.proposalService.validateInProgressState(proposal);
						break;
					case IN_COMMERCIAL_APPROVAL:
						this.proposalService.validateInCommercialApprovalState(proposal);
						break;
					case COMMERCIAL_DISAPPROVED:
						this.proposalService.validateCommercialDisapprovedState(proposal);
						break;
					case COMMERCIAL_APPROVED:
						this.proposalService.validateCommercialApprovedState(proposal);
						break;
					case ON_CUSTOMER_APPROVAL:
						this.proposalService.validateOnCustomerApprovalState(proposal);
						break;
					case FINISHED_WITHOUT_SALE:
						this.proposalService.validateFinishedWithoutSaleState(proposal);
						break;
					case FINISHED_WITH_SALE:
						this.proposalService.validateFinishedWithSaleState(proposal);
						break;
					case CANCELED:
						this.proposalService.validateCanceledState(proposal);
						break;
					default:
						throw new BusException( "O status " + target + " ?? inv??lido ou n??o existe." );
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

	private StateMachineListener<ProposalState, ProposalEvents> listener( String id ) {
		return new StateMachineListenerAdapter<ProposalState, ProposalEvents>() {
			@Override
			public void stateChanged(State<ProposalState, ProposalEvents> from, State<ProposalState, ProposalEvents> to) {
				log.debug( "ProposalState-stateChange ID: {} - From {} to {}", id, (from == null ? "N/A" : from.getId() ), (to == null ? "N/A" : to.getId() ) );
			}
			
			@Override
			public void stateMachineError(StateMachine<ProposalState, ProposalEvents> stateMachine, Exception exception) {
				log.warn( "ProposalState-stateMachineError ID: {} SM: {}", id, stateMachine, exception );
			}
			
			@SuppressWarnings("rawtypes")
			@Override
		    public void eventNotAccepted( Message event ) {
				log.warn( "ProposalState-eventNotAccepted ID: {} Event: {}", id, event );
		    }
		};
	}

	/**
	 * Recupera a m??quina de estado com base no estado passado no argumento
	 *  
	 * @param id ID que ser dado a m??quina de status	
	 * @param state estado a ser carregado como de partida da m??quina.	
	 * @return inst??ncia da m??quina de estado.
	 */
	public StateMachine<ProposalState, ProposalEvents> recoveryStateMachine( String id, ProposalState state ) throws BusException, AppException {
		if( state == null) {
			throw new AppException( "N??o ?? poss??vel carregar a m??quina com o estado inv??lido." ); 
		}
		
		try {
			StateMachine<ProposalState, ProposalEvents> sm = this.buildNewStateMachine( id );
			sm.stop();
			
			sm.getStateMachineAccessor()
					.doWithAllRegions( sma -> {
						sma.resetStateMachine( new DefaultStateMachineContext<>( state , null, null, null) );
					} );
			
			sm.start();
			
			return sm;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar a m??quina de estado do lead.", e );
			throw new AppException( e );
		}
	}

	/**
	 * Valida se a transi????o entre 2 status ?? v??lida.
	 * 
	 * @param sm			inst??ncia da m??quina de status
	 * @param source		status de origem para a valida????o
	 * @param target		status de destino para a valida????o
	 * @return				true se a troca entre os status ?? v??lida, false caso n??o seja v??lido
	 * @throws BusException
	 * @throws AppException
	 */
	public static boolean isStateTransitionValid( StateMachine<ProposalState, ProposalEvents> sm, ProposalState source, ProposalState target ) throws BusException, AppException {
		
		if( sm == null ) {
			throw new AppException( "M??quina de estado inv??lida." );
		}
		
		if( source == null || target == null ) {
			throw new AppException( "Status de origiem e/ou destino inv??lidos." );
		}
		
		if( sm.getTransitions() != null ) {
			
			Map<ProposalState, List<ProposalState>> map = sm.getTransitions().stream()
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
	 * Com base na m??quina de estado passada montamos a lista de ORIGEM / DESTINO e A????O que ?? poss??vel ser executado.
	 * 
	 * @param sm			 m??quina de estado da Proposta
	 * @return				 lista com as combina????es de ORIGEM, DESTINO e A????O
	 * @throws BusException
	 * @throws AppException
	 */
	public static List<ProposalWorkflow> getStateWorkflowTree( StateMachine<ProposalState, ProposalEvents> sm ) throws BusException, AppException {
		
		if( sm == null ) {
			throw new AppException( "M??quina de estado inv??lida." );
		}
		
		List<ProposalWorkflow> tree = new ArrayList<>();
		
		if( sm.getTransitions() != null ) {
			
			sm.getTransitions().forEach( t -> {
				tree.add(
						ProposalWorkflow.builder()
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
