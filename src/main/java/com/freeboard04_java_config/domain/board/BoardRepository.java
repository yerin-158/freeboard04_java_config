package com.freeboard04_java_config.domain.board;

import com.freeboard04_java_config.domain.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long>, JpaSpecificationExecutor {
    List<BoardEntity> findAllByWriterId(long writerId);
    Page<BoardEntity> findAllByWriterIn(List<UserEntity> userEntityList, Pageable pageable);
    Page<BoardEntity> findAll(Specification spec, Pageable pageable);
}
