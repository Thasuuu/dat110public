package no.hvl.dat110.transport.rdt2;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import no.hvl.dat110.transport.*;

public class TransportReceiverRDT2 extends TransportReceiver implements ITransportProtocolEntity {

	public enum RDT2ReceiverStates {
		WAITING;
	}
	
	private RDT2ReceiverStates state;
	
	private LinkedBlockingQueue<SegmentRDT2> inqueue;

	public TransportReceiverRDT2() {
		super("TransportReceiver");
		state = RDT2ReceiverStates.WAITING;
		inqueue = new LinkedBlockingQueue<SegmentRDT2>();
	}
	
	// network service will call this method when segments arrive
	public final void rdt_recv(Segment segment) {

		System.out.println("[Transport:Receiver ] rdt_recv: " + segment.toString());

		try {
			inqueue.put((SegmentRDT2)segment);
		} catch (InterruptedException ex) {

			System.out.println("Transport receiver  " + ex.getMessage());
			ex.printStackTrace();
		}

	}
	
	public void doProcess() {

		SegmentRDT2 segment = null;

		switch (state) {

		case WAITING:

			try {

				segment = inqueue.poll(2, TimeUnit.SECONDS);

			} catch (InterruptedException ex) {
				System.out.println("TransportReceiver RDT2 - doProcess " + ex.getMessage());
				ex.printStackTrace();
			}
			
			if (segment != null) {

				Segment acksegment;

				if (segment.isCorrect()) {

					// deliver data to the transport layer
					deliver_data(segment.getData());

					// send an ack to the sender
					acksegment = new SegmentRDT2(SegmentType.ACK);
					
				} else {
					// send an ack to the sender
					acksegment = new SegmentRDT2(SegmentType.NAK);
				}

				udt_send(acksegment);
			}

			break;
		default:
			break;
		}
	}
}
