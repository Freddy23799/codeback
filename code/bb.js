// import http from 'k6/http';
// import { check, sleep } from 'k6';
// import { Rate } from 'k6/metrics';

// export let errorRate = new Rate('errors');

// export let options = {
//     duration: '3m',
//     vus: 50, // utilisateurs virtuels
//     thresholds: {
//         errors: ['rate<0.1'],
//         http_req_duration: ['p(95)<500']
//     }
// };

// // Ton token JWT ici
// const TOKEN = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJGUkFOQ0siLCJyb2xlcyI6WyJST0xFX1NVUlZFSUxMQU5UIl0sImlhdCI6MTc1OTI5MTM1OSwiZXhwIjoxNzU5MzI3MzU5fQ.v8u4T4yi5xCMG7Kn8waPXqVKp8o0oGQRC0VbgN1OJUQ';

// // Tous les endpoints à tester
// const endpoints = [
//     '/api/admin/academic-years',
//     '/api/admin/academic-years/1',
//     '/api/admin/professors',
//     '/api/admin/stats',
//     '/api/admin/enrollments-by-level',
//     '/api/admin/teachers/1',
//     '/api/admin/surveillant/1',
//     '/api/admin/responsable/1',
//     ,
//     '/api/admin/sessions/1/students',
   
//     '/api/admin/teacher',
//     '/api/admin/surveillant',
//     '/api/admin/responsable',
   
//     '/api/admin/students/1',
   
//     '/api/admin/admins/1',
//     '/api/admin/me',
//     '/api/admin/teacher-session',
//     '/api/admin/teacher/1/sessions',
//     '/api/admin/session/1/students',
//     '/api/sessions/1',
//     '/api/sessions/surveillant',
//     '/api/sessions',
//     '/api/sessions/1',
   
//     '/api/sessions/scan/bulk',
//     '/api/sessions/1/markManual',
//     '/api/sessions/1/expectedProfiles',
  
// ];

// export default function () {
//     endpoints.forEach(url => {
//         let res = http.get(`http://localhost:9001${url}`, {
//             headers: {
//                 Authorization: TOKEN,
//                 'Content-Type': 'application/json'
//             }
//         });

//         check(res, {
//             'status is 200': (r) => r.status === 200,
//         }) || errorRate.add(1);

//         sleep(Math.random() * 2 + 1); 
//     });
// }
