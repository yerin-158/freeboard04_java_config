package com.freeboard04_java_config.domain.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourlyPopularBoardRepository extends JpaRepository<HourlyPopularBoardEntity, Long> {
}
