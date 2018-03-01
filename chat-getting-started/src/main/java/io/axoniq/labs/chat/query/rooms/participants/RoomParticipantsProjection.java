package io.axoniq.labs.chat.query.rooms.participants;

import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/rooms/{roomId}/participants")
public class RoomParticipantsProjection {

    private final RoomParticipantsRepository repository;

    public RoomParticipantsProjection(RoomParticipantsRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<String> participantsInRoom(@PathVariable String roomId) {
        return repository.findRoomParticipantsByRoomId(roomId)
                .stream()
                .map(RoomParticipant::getParticipant).sorted().collect(toList());
    }

    @Transactional
    @EventHandler
    public void on(ParticipantJoinedRoomEvent e) {
        repository.save(new RoomParticipant(e.getRoomId(), e.getParticipant()));
    }

    @Transactional
    @EventHandler
    public void on(ParticipantLeftRoomEvent e) {
        repository.deleteByParticipantAndRoomId(e.getParticipant(), e.getRoomId());

    }


}
