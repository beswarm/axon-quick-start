package io.axoniq.labs.chat.query.rooms.summary;

import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomSummaryProjection {

    private final RoomSummaryRepository roomSummaryRepository;

    public RoomSummaryProjection(RoomSummaryRepository roomSummaryRepository) {
        this.roomSummaryRepository = roomSummaryRepository;
    }

    @GetMapping
    public List<RoomSummary> listRooms() {
        return roomSummaryRepository.findAll();
    }

    @Transactional
    @EventHandler
    public void on(ParticipantJoinedRoomEvent e) {
        RoomSummary summary = roomSummaryRepository.getOne(e.getRoomId());
        summary.addParticipant();
        roomSummaryRepository.save(summary);
    }

    @Transactional
    @EventHandler
    public void on(ParticipantLeftRoomEvent e) {
        RoomSummary summary = roomSummaryRepository.getOne(e.getRoomId());
        summary.removeParticipant();
        roomSummaryRepository.save(summary);
    }

    @Transactional
    @EventHandler
    public void on(RoomCreatedEvent e) {
        roomSummaryRepository.save(new RoomSummary(e.getRoomId(), e.getName()));
    }

}
