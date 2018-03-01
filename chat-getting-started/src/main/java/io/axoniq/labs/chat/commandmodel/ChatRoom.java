package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.HashSet;
import java.util.Set;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class ChatRoom {
    @AggregateIdentifier
    private String roomId;

    private Set<String> participants = new HashSet<>();

    public ChatRoom() {
    }

    @CommandHandler
    public ChatRoom(CreateRoomCommand cmd) {
        apply(new RoomCreatedEvent(cmd.getRoomId(), cmd.getName()));
    }


    @EventSourcingHandler
    public void on(RoomCreatedEvent roomCreatedEvent) {
        this.roomId = roomCreatedEvent.getRoomId();
    }


    @CommandHandler
    public void handle(JoinRoomCommand command) {
        if (!participants.contains(command.getParticipant())) {
            apply(new ParticipantJoinedRoomEvent(command.getParticipant(), command.getRoomId()));
        }
    }

    @EventSourcingHandler
    public void on(ParticipantJoinedRoomEvent event) {
        participants.add(event.getParticipant());

    }


    @CommandHandler
    public void handle(PostMessageCommand cmd) {
        if (participants.contains(cmd.getParticipant())) {
            apply(new MessagePostedEvent(cmd.getParticipant(), cmd.getRoomId(), cmd.getMessage()));
        } else {
            throw new IllegalStateException("can not post message if not in room");
        }
    }

    @EventSourcingHandler
    public void on(MessagePostedEvent event) {

    }

    @CommandHandler
    public void handle(LeaveRoomCommand cmd) {
        if (this.participants.contains(cmd.getParticipant())) {
            apply(new ParticipantLeftRoomEvent(cmd.getParticipant(), cmd.getRoomId()));
        }
    }

    @EventSourcingHandler
    public void on(ParticipantLeftRoomEvent event) {
        this.participants.remove(event.getParticipant());
    }

}
