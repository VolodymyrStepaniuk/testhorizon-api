package com.stepaniuk.testhorizon.notebook.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<Note, Long> , JpaSpecificationExecutor<Note> {
}