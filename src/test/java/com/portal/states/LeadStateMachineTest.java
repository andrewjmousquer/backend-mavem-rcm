package com.portal.states;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.portal.dao.impl.LeadDAO;
import com.portal.enums.LeadEvents;
import com.portal.enums.LeadState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.exceptions.StateWorkflowException;
import com.portal.model.Lead;
import com.portal.service.imp.LeadService;
import com.portal.utils.LeadStateBuilder;

@ExtendWith(SpringExtension.class)
class LeadStateMachineTest {

	@Mock
	LeadDAO dao;

	@Mock
	LeadService service;
	
	@InjectMocks
	LeadStateBuilder builder;
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CANCELED")
	void givenIniitalState_whenChangeToCanceled_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateCanceledState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-1" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CANCEL )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CANCELED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CONTACTED -> CONVERTED")
	void givenIniitalState_whenChangeToConverted_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-2" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONVERTED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CONTACTED -> UNCONVERTED")
	void givenIniitalState_whenChangeToUnconverted_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateUnConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-3" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.NOT_CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.UNCONVERTED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CONTACTED -> UNCONVERTED -> CANCELED")
	void givenIniitalState_whenChangeToUnconvertedAndCanceled_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateUnConvertedState(any());
		doNothing().when( spy(service ) ).validateCanceledState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-4" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.NOT_CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.UNCONVERTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CANCEL )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CANCELED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CONTACTED -> CONVERTED -> CANCELED")
	void givenIniitalState_whenChangeToConvertedAndCanceled_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateConvertedState(any());
		doNothing().when( spy(service ) ).validateCanceledState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-5" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONVERTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CANCEL )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CANCELED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CONTACTED -> CONVERTED -> UNCONVERTED")
	void givenIniitalState_whenChangeToConvertedAndUnconverted_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateConvertedState(any());
		doNothing().when( spy(service ) ).validateUnConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-6" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONVERTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.NOT_CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.UNCONVERTED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo OPENED -> CONTACTED -> UNCONVERTED -> CONVERTED")
	void givenIniitalState_whenChangeToUnconvertedAndConverted_thenNoError() throws Exception {
		
		doNothing().when( spy(service ) ).validateUnConvertedState(any());
		doNothing().when( spy(service ) ).validateConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-7" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.NOT_CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.UNCONVERTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
								.withPayload( LeadEvents.CONVERT )
								.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
								.build() )
						.expectState( LeadState.CONVERTED )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo inválido OPENED -> UNCONVERTED")
	void givenIniitalState_whenChangeToUnconverted_thenError() throws Exception {
		
		doNothing().when( spy(service ) ).validateUnConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-8" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.NOT_CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectEventNotAccepted( 1 )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo inválido OPENED -> CONVERTED")
	void givenIniitalState_whenChangeToConverted_thenError() throws Exception {
		
		doNothing().when( spy(service ) ).validateConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-9" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONVERT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectEventNotAccepted( 1 )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo inválido OPENED -> CONTACTED -> CONVERTED -> CONTACTED")
	void givenIniitalState_whenChangeToCanceledToContactedToConvertedToContacted_thenError() throws Exception {
		
		doNothing().when( spy(service ) ).validateCanceledState(any());
		doNothing().when( spy(service ) ).validateContactedState(any());
		doNothing().when( spy(service ) ).validateConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-10" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
								.withPayload( LeadEvents.CONVERT )
								.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
								.build() )
						.expectState( LeadState.CONVERTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
								.withPayload( LeadEvents.CONTACT )
								.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
								.build() )
						.expectEventNotAccepted( 1 )
						.and()
					.build();
		
		plan.test();
	}
	
	@Test
	@DisplayName("Dado o fluxo inválido OPENED -> CONTACTED -> UNCONVERTED -> CONTACTED")
	void givenIniitalState_whenChangeToCanceledToContactedToUnconvertedToContacted_thenError() throws Exception {
		
		doNothing().when( spy(service ) ).validateCanceledState(any());
		doNothing().when( spy(service ) ).validateContactedState(any());
		doNothing().when( spy(service ) ).validateUnConvertedState(any());
		
		StateMachine<LeadState, LeadEvents> machine = this.builder.buildNewStateMachine( "teste-10" );
		
		StateMachineTestPlan<LeadState, LeadEvents> plan =
				StateMachineTestPlanBuilder.<LeadState, LeadEvents>builder()
					.stateMachine( machine )
					.step()
						.expectState( LeadState.OPENED )
						.and()
					.step()
						.sendEvent( MessageBuilder
										.withPayload( LeadEvents.CONTACT )
										.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
										.build() )
						.expectState( LeadState.CONTACTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
								.withPayload( LeadEvents.NOT_CONVERT )
								.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
								.build() )
						.expectState( LeadState.UNCONVERTED )
						.and()
					.step()
						.sendEvent( MessageBuilder
								.withPayload( LeadEvents.CONTACT )
								.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
								.build() )
						.expectEventNotAccepted( 1 )
						.and()
					.build();
		
		plan.test();
	}
	
	@ParameterizedTest
	@DisplayName("Dados os status valida se a troca é válida")
	@MethodSource( "com.portal.states.LeadStateMachineTest#validStatesChange" )
	void validFlowTest( LeadState source, LeadState target ) throws Exception {
		StateMachine<LeadState, LeadEvents> sm = this.builder.buildNewStateMachine();
		assertTrue( LeadStateBuilder.isStateTransitionValid(sm, source, target) );
	}
	
	@ParameterizedTest
	@DisplayName("Dados os status valida se a troca é válida")
	@MethodSource( "com.portal.states.LeadStateMachineTest#invalidStatesChange" )
	void invalidFlowTest( LeadState source, LeadState target ) throws Exception {
		StateMachine<LeadState, LeadEvents> sm = this.builder.buildNewStateMachine();
		assertFalse( LeadStateBuilder.isStateTransitionValid(sm, source, target) );
	}
	
	@ParameterizedTest
	@DisplayName("Teste de fluxos válidos iniciados em vários no meio")
	@MethodSource( "com.portal.states.LeadStateMachineTest#validStatesTransitions" )
	void validFlowTest( LeadState source, LeadEvents event, LeadState target ) throws StateWorkflowException, BusException, AppException {
		StateMachine<LeadState, LeadEvents> sm = this.builder.recoveryStateMachine( UUID.randomUUID().toString(), source );
		
		Message<LeadEvents> msg = MessageBuilder
									.withPayload( event )
									.setHeader( LeadStateBuilder.LEAD_INSTANCE, Lead.builder().id(0).build() )
									.build();
		
		
		assertEquals( source, sm.getState().getId() );
		
		boolean eventSuccess = sm.sendEvent( msg );
		
		assertTrue( eventSuccess );
		assertFalse( sm.hasStateMachineError() );
		assertEquals( target, sm.getState().getId() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> validStatesTransitions() {
	    return Stream.of(
	    		Arguments.of( LeadState.OPENED, LeadEvents.OPEN, LeadState.OPENED  ),
				Arguments.of( LeadState.OPENED, LeadEvents.CONTACT, LeadState.CONTACTED  ),
				Arguments.of( LeadState.OPENED, LeadEvents.CANCEL, LeadState.CANCELED  ),
				Arguments.of( LeadState.CONTACTED, LeadEvents.CONTACT, LeadState.CONTACTED ),
				Arguments.of( LeadState.CONTACTED, LeadEvents.CONVERT, LeadState.CONVERTED ),
				Arguments.of( LeadState.CONTACTED, LeadEvents.NOT_CONVERT, LeadState.UNCONVERTED ),
				Arguments.of( LeadState.CONTACTED, LeadEvents.CANCEL, LeadState.CANCELED ),
				Arguments.of( LeadState.CONVERTED, LeadEvents.CONVERT, LeadState.CONVERTED ),
				Arguments.of( LeadState.CONVERTED, LeadEvents.NOT_CONVERT, LeadState.UNCONVERTED ),
				Arguments.of( LeadState.CONVERTED, LeadEvents.CANCEL, LeadState.CANCELED ),
				Arguments.of( LeadState.UNCONVERTED, LeadEvents.NOT_CONVERT, LeadState.UNCONVERTED ),
				Arguments.of( LeadState.UNCONVERTED, LeadEvents.CONVERT, LeadState.CONVERTED ),
				Arguments.of( LeadState.UNCONVERTED, LeadEvents.CANCEL, LeadState.CANCELED )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> validStatesChange() {
	    return Stream.of(
	    		Arguments.of( LeadState.OPENED, LeadState.OPENED  ),
				Arguments.of( LeadState.OPENED, LeadState.CONTACTED  ),
				Arguments.of( LeadState.OPENED, LeadState.CANCELED  ),
				Arguments.of( LeadState.CONTACTED, LeadState.CONTACTED ),
				Arguments.of( LeadState.CONTACTED, LeadState.CONVERTED ),
				Arguments.of( LeadState.CONTACTED, LeadState.UNCONVERTED ),
				Arguments.of( LeadState.CONTACTED, LeadState.CANCELED ),
				Arguments.of( LeadState.CONVERTED, LeadState.CONVERTED ),
				Arguments.of( LeadState.CONVERTED, LeadState.UNCONVERTED ),
				Arguments.of( LeadState.CONVERTED, LeadState.CANCELED ),
				Arguments.of( LeadState.UNCONVERTED, LeadState.UNCONVERTED ),
				Arguments.of( LeadState.UNCONVERTED, LeadState.CONVERTED ),
				Arguments.of( LeadState.UNCONVERTED, LeadState.CANCELED )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidStatesChange() {
	    return Stream.of(
	    		Arguments.of( LeadState.OPENED, LeadState.CONVERTED  ),
				Arguments.of( LeadState.OPENED, LeadState.UNCONVERTED  ),
				Arguments.of( LeadState.CONTACTED, LeadState.OPENED ),
				Arguments.of( LeadState.CONVERTED, LeadState.OPENED ),
				Arguments.of( LeadState.CONVERTED, LeadState.CONTACTED ),
				Arguments.of( LeadState.UNCONVERTED, LeadState.OPENED ),
				Arguments.of( LeadState.UNCONVERTED, LeadState.CONTACTED ),
				Arguments.of( LeadState.CANCELED, LeadState.OPENED ),
				Arguments.of( LeadState.CANCELED, LeadState.CONTACTED ),
				Arguments.of( LeadState.CANCELED, LeadState.CONVERTED ),
				Arguments.of( LeadState.CANCELED, LeadState.UNCONVERTED )
	    );
	}
}

