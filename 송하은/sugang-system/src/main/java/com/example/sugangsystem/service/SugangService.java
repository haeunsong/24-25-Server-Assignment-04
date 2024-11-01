package com.example.sugangsystem.service;

import com.example.sugangsystem.domain.Course;
import com.example.sugangsystem.domain.Student;
import com.example.sugangsystem.domain.Sugang;
import com.example.sugangsystem.dto.request.sugang.RegisterSugangRequestDto;
import com.example.sugangsystem.dto.response.sugang.GetCountByCourseResponseDto;
import com.example.sugangsystem.dto.response.sugang.GetSugangByStudentIdResponseDto;
import com.example.sugangsystem.dto.response.sugang.RegisterSugangResponseDto;
import com.example.sugangsystem.repository.CourseRepository;
import com.example.sugangsystem.repository.StudentRepository;
import com.example.sugangsystem.repository.SugangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SugangService {

    private final SugangRepository sugangRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // 학생이 강의를 수강신청
    @Transactional
    public RegisterSugangResponseDto register(RegisterSugangRequestDto registerSugangRequestDto) {
       // 각각의 ID를 통해 Student 와 Course 정보를 먼저 받아온다.
        Student student = studentRepository.findById(registerSugangRequestDto.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));
        Course course = courseRepository.findById(registerSugangRequestDto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 중복 수강신청 여부 체크
        if(sugangRepository.existsByStudentAndCourse(student, course)){
            throw new IllegalStateException("이미 신청한 강의입니다.");
        }

        Sugang sugang = Sugang.createSugang(student,course);
        sugangRepository.save(sugang);

        return RegisterSugangResponseDto.from(sugang);
    }

    // 학생 id 로 해당 수강신청 목록 조회
    @Transactional(readOnly = true)
    public List<GetSugangByStudentIdResponseDto> getSugangListByStudentId(Long studentId) {
        List<Sugang> sugangList = sugangRepository.findByStudent_Id(studentId);

        return sugangList.stream()
                .map(GetSugangByStudentIdResponseDto::from)
                .toList();
    }

    // 학생이 원하는 수강신청 삭제
    @Transactional
    public void cancelSugang(Long studentId, Long sugangId) {
        Sugang sugang = sugangRepository.findByIdAndStudent_Id(sugangId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수강내역입니다."));
        sugangRepository.delete(sugang);
    }

    // 추가 기능 - 강의별로 수강신청 인원 구하기 (통계)
    @Transactional(readOnly = true)
    public List<GetCountByCourseResponseDto> getCountByCourse() {
        List<Map<String,Object>> statistics = sugangRepository.countSugangsByCourse();

        return statistics.stream()
                .map(GetCountByCourseResponseDto::from)
                .toList();
    }
}
