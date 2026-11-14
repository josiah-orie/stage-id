package com.jovine360.talenthub.scouting.application.service;

import com.jovine360.talenthub.scouting.domain.model.ScoutNote;
import com.jovine360.talenthub.scouting.domain.repository.ScoutNoteRepository;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** A scout's own notes on athletes. Only the author can read or delete them. */
@Service
@Transactional
public class ScoutNoteService {

    private final ScoutNoteRepository noteRepository;

    public ScoutNoteService(ScoutNoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public ScoutNote create(String scoutProfileId, String athleteId, String text, LocalDate noteDate, Boolean isPrivate) {
        ScoutNote n = new ScoutNote();
        n.setScoutProfileId(scoutProfileId);
        n.setAthleteId(athleteId);
        n.setNoteText(text);
        n.setNoteDate(noteDate != null ? noteDate : LocalDate.now());
        n.setPrivate(isPrivate == null || isPrivate);
        return noteRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<ScoutNote> list(String scoutProfileId, String athleteId) {
        return athleteId == null || athleteId.isBlank()
                ? noteRepository.findByScoutProfileIdOrderByCreatedAtDesc(scoutProfileId)
                : noteRepository.findByScoutProfileIdAndAthleteIdOrderByCreatedAtDesc(scoutProfileId, athleteId);
    }

    public void delete(String noteId, String scoutProfileId) {
        ScoutNote n = noteRepository.findById(noteId)
                .filter(x -> x.getScoutProfileId().equals(scoutProfileId))
                .orElseThrow(() -> new EntityNotFoundException("ScoutNote", noteId));
        noteRepository.delete(n);
    }
}
