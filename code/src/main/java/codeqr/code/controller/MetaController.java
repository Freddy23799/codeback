// package codeqr.code.controller;

// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import codeqr.code.service.AcademicYearService;
// import codeqr.code.service.LevelService;
// import codeqr.code.service.RoomService;
// import codeqr.code.service.CampusService;
// import codeqr.code.service.CourseService;
// import codeqr.code.service.SpecialtyService; // si tu as un service pour specialties

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/prof")
// @RequiredArgsConstructor
// @CrossOrigin
// public class MetaController {

//     private final AcademicYearService academicYearService;
//     private final LevelService levelService;
//     private final RoomService roomService;
//     private final CampusService campusService;
//     private final CourseService courseService;
//     private final SpecialtyService specialtyService; // ajouter si tu as

//     @GetMapping("/meta")
//     public ResponseEntity<Map<String, Object>> getMeta() {
//         Map<String, Object> meta = new HashMap<>();
//         meta.put("academicYears", academicYearService.findAll());
//         meta.put("levels", levelService.findAll());
//         meta.put("rooms", roomService.findAll());
//         meta.put("campuses", campusService.findAll());
//         meta.put("courses", courseService.findAll());
//         meta.put("specialties", specialtyService.findAll()); // ajouter si tu as
//         return ResponseEntity.ok(meta);
//     }
// }
