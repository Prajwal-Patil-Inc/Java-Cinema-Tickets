import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpTest {
    private final TicketPaymentService paymentService = mock(TicketPaymentService.class);
    private final SeatReservationService reservationService = mock(SeatReservationService.class);
    private final TicketService ticketService = new TicketServiceImpl(paymentService, reservationService);

    @Test
    public void testValidPurchase1(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(Type.CHILD, 1);
        
        ticketService.purchaseTickets(1L, adultTicket, childTicket);

        verify(paymentService).makePayment(1L, 65);
        verify(reservationService).reserveSeat(1L, 3);
    }

    @Test
    public void testValidPurchase2(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, 12);
        TicketTypeRequest childTicket = new TicketTypeRequest(Type.CHILD, 6);
        TicketTypeRequest infantTicket = new TicketTypeRequest(Type.INFANT, 2);
        
        ticketService.purchaseTickets(123L, adultTicket, childTicket, infantTicket);

        verify(paymentService).makePayment(123L, 390);
        verify(reservationService).reserveSeat(123L, 18);
    }

    @Test
    public void test25TicketsPurchase(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, 17);
        TicketTypeRequest childTicket = new TicketTypeRequest(Type.CHILD, 7);
        TicketTypeRequest infantTicket = new TicketTypeRequest(Type.INFANT, 1);
        
        ticketService.purchaseTickets(123L, adultTicket, childTicket, infantTicket);

        verify(paymentService).makePayment(123L, 530);
        verify(reservationService).reserveSeat(123L, 24);
    }

    @Test
    public void testOnlyAdultsTicketsPurchase(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, 1);

        ticketService.purchaseTickets(123L, adultTicket);

        verify(paymentService).makePayment(123L, 25);
        verify(reservationService).reserveSeat(123L, 1);
    }

    @Test
    public void testNegativeTicketPurchase(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, -2);
        TicketTypeRequest childTicket = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest infantTicket = new TicketTypeRequest(Type.INFANT, 2);
        
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(123L, adultTicket, childTicket, infantTicket);
        });
    }

    @Test
    public void testZeroTicketPurchase(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, 0);
        TicketTypeRequest childTicket = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest infantTicket = new TicketTypeRequest(Type.INFANT, 2);
        
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(123L, adultTicket, childTicket, infantTicket);
        });
    }

    @Test
    public void testPurchaseMoreThan25Tickets(){
        TicketTypeRequest adultTicket = new TicketTypeRequest(Type.ADULT, 26);

        assertThrows(InvalidPurchaseException.class, ()->{
            ticketService.purchaseTickets(1L, adultTicket);
        });
    }

    @Test
    public void testZeroTicketsRequest(){
        assertThrows(InvalidPurchaseException.class, ()->{
            ticketService.purchaseTickets(1L);
        });
    }

     @Test
    public void testNullTicketTypeRequest(){        
        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(123L, null);
        });
    }

    @Test
    public void testPurchaseWithoutAdult(){
        TicketTypeRequest childTickets = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest infantTickets = new TicketTypeRequest(Type.INFANT, 1);

        assertThrows(InvalidPurchaseException.class, ()->{
            ticketService.purchaseTickets(1L, childTickets, infantTickets);
        });
    }

    @Test
    public void testAdultToInfantRatio(){
        TicketTypeRequest adultTickets = new TicketTypeRequest(Type.ADULT, 1);
        TicketTypeRequest infantTickets = new TicketTypeRequest(Type.INFANT, 1);

        ticketService.purchaseTickets(1L, adultTickets, infantTickets);

        verify(paymentService).makePayment(1L, 25);
        verify(reservationService).reserveSeat(1L, 1); 
    }

    @Test
    public void testInvalidAdultToInfantRatio(){
        TicketTypeRequest adultTickets = new TicketTypeRequest(Type.ADULT, 1);
        TicketTypeRequest infantTickets = new TicketTypeRequest(Type.INFANT, 3);

        assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(1L, adultTickets, infantTickets);
        });
    }
    
    @Test
    public void testNullAccountID(){
        TicketTypeRequest adultTickets = new TicketTypeRequest(Type.ADULT, 2);

        assertThrows(InvalidPurchaseException.class, ()->{
            ticketService.purchaseTickets(null, adultTickets);
        });
    }

    @Test
    public void testInvalidAccountID(){
        TicketTypeRequest adultTickets = new TicketTypeRequest(Type.ADULT, 2);

        assertThrows(InvalidPurchaseException.class, ()->{
            ticketService.purchaseTickets(-1L, adultTickets);
        });
    }
}
