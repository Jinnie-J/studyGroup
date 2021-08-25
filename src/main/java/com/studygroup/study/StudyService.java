package com.studygroup.study;

import com.studygroup.domain.Account;
import com.studygroup.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;

    public Study createNewStudy(Study study, Account account){
        Study newStudy = repository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }
}
