package com.chung.lifusic.file.repository;

import com.chung.lifusic.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
