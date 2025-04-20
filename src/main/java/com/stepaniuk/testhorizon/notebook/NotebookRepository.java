package com.stepaniuk.testhorizon.notebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotebookRepository extends JpaRepository<Notebook, Long>, JpaSpecificationExecutor<Notebook> {
}