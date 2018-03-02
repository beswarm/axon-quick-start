package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
@Slf4j
public class ChatRoom {

    @AggregateIdentifier
    private String roomId;
    private Set<String> participants;

    public ChatRoom() {
    }

    @CommandHandler
    public ChatRoom(CreateRoomCommand command) {
        log.info("create room {}, cmd: {}", command.getRoomId(), command);
        apply(new RoomCreatedEvent(command.getRoomId(), command.getName()));
    }

    @CommandHandler
    public void handle(JoinRoomCommand command) {
        log.info("{} join room {}, cmd :{}", command.getParticipant(), command.getRoomId(), command);
        if (!participants.contains(command.getParticipant())) {
            apply(new ParticipantJoinedRoomEvent(command.getParticipant(), roomId));
        }
    }

    @CommandHandler
    public void handle(LeaveRoomCommand command) {
        log.info("{} leave room {}, cmd: {}", command.getParticipant(), command.getRoomId(), command);
        if (participants.contains(command.getParticipant())) {
            apply(new ParticipantLeftRoomEvent(command.getParticipant(), roomId));
        }
    }

    @CommandHandler
    public void handle(PostMessageCommand command) {
        log.info("{} post message  {}, cmd: {}", command.getParticipant(), command.getMessage(), command);
        Assert.state(participants.contains(command.getParticipant()),
                "You cannot post messages unless you've joined the chat room");
        apply(new MessagePostedEvent(command.getParticipant(), roomId, command.getMessage()));
    }

    @EventSourcingHandler
    protected void on(RoomCreatedEvent event) {
        this.roomId = event.getRoomId();
        this.participants = new HashSet<>();
    }

    @EventSourcingHandler
    protected void on(ParticipantJoinedRoomEvent event) {
        this.participants.add(event.getParticipant());
    }

    @EventSourcingHandler
    protected void on(ParticipantLeftRoomEvent event) {
        this.participants.remove(event.getParticipant());
    }
}
