package com.example.repo;

import com.example.model.MoonMission;

import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {
    List<String> listSpacecraft();

    Optional<MoonMission> findById(int id);

    int countByYear(int year);
}


