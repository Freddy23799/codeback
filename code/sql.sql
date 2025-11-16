INSERT INTO `level`(`id`, `name`) VALUES 
(1, 'Licence 1'),
(2, 'Licence 2'),
(3, 'Licence 3'),
(4, 'Master 1'),
(5, 'Master 2');





INSERT INTO `department`(`id`, `name`) VALUES
(1, 'Informatique'),
(2, 'Mathématiques'),
(3, 'Physique'),
(4, 'Chimie'),
(5, 'Biologie'),
(6, 'Économie'),
(7, 'Droit'),
(8, 'Lettres'),
(9, 'Histoire'),
(10, 'Génie Civil');



INSERT INTO `campus`(`address`, `name`) VALUES
('Finance', 'Campus D'),
('Camoco', 'Campus C'),
('École Normale', 'Campus E'),
('Makambou', 'Campus F'),
('Lycée Classique', 'Campus A');




INSERT INTO `room`(`capacity`, `name`, `campus_id`) VALUES
(400, 'EMPHI', 2),
(50, 'C512', 2),
(30, 'C510', 2),
(30, 'C610', 2),
(60, 'D510', 1),
(20, 'D312', 1),
(20, 'E200', 3),
(25, 'E202', 3),
(50, 'A500', 4),
(50, 'A510', 4);



INSERT INTO `specialty`(`name`, `department_id`) VALUES
('Développement Web', 1),
('Intelligence Artificielle', 1),
('Algèbre et Analyse', 2),
('Statistiques et Probabilités', 2),
('Physique Théorique', 3),
('Physique Appliquée', 3),
('Chimie Organique', 4),
('Chimie Inorganique', 4),
('Biotechnologie', 5),
('Microbiologie', 5),
('Économie Internationale', 6),
('Finance et Comptabilité', 6),
('Droit Constitutionnel', 7),
('Droit International', 7),
('Littérature Française', 8),
('Littérature Comparée', 8),
('Histoire Moderne', 9),
('Archéologie', 9),
('Génie Civil et Construction', 10),
('Génie Urbain', 10);




INSERT INTO `academic_year`(`active`, `end_date`, `label`, `start_date`) VALUES
(1, '2025-06-30', '2024/2025', '2024-09-01'),
(0, '2024-06-30', '2023/2024', '2023-09-01'),
(0, '2023-06-30', '2022/2023', '2022-09-01'),
(0, '2022-06-30', '2021/2022', '2021-09-01'),
(0, '2021-06-30', '2020/2021', '2020-09-01');





















































1️⃣ Créer un étudiant (POST /api/students)

URL :

POST http://localhost:8080/api/students


Headers :

Content-Type: application/json
Authorization: Bearer <TOKEN_ADMIN>


Body (raw JSON) :

{
  "fullName": "John Doe",
  "email": "johndoe@example.com",
  "user": {
    "username": "john_doe",
    "password": "password123"
  }
}


Réponse attendue (200 OK) :

{
  "id": 1,
  "fullName": "John Doe",
  "email": "johndoe@example.com",
  "user": {
    "id": 1,
    "username": "john_doe",
    "role": "ETUDIANT"
  }
}

2️⃣ Mettre à jour un étudiant (PUT /api/students/{id})

URL :

PUT http://localhost:8080/api/students/1


Headers :

Content-Type: application/json
Authorization: Bearer <TOKEN_ADMIN>


Body (raw JSON) :

{
  "fullName": "John Doe Updated",
  "email": "johnupdated@example.com",
  "user": {
    "username": "john_doe",
    "password": "newpassword123"
  }
}


Réponse attendue (200 OK) :

{
  "id": 1,
  "fullName": "John Doe Updated",
  "email": "johnupdated@example.com",
  "user": {
    "id": 1,
    "username": "john_doe",
    "role": "ETUDIANT"
  }
}

3️⃣ Récupérer tous les étudiants (GET /api/students/all)

URL :

GET http://localhost:8080/api/students/all


Headers :

Authorization: Bearer <TOKEN_ADMIN>


Réponse attendue (200 OK) :

[
  {
    "id": 1,
    "fullName": "John Doe Updated",
    "email": "johnupdated@example.com",
    "user": {
      "id": 1,
      "username": "john_doe",
      "role": "ETUDIANT"
    }
  }
]

4️⃣ Récupérer un étudiant par ID (GET /api/students/{id})

URL :

GET http://localhost:8080/api/students/1


Headers :

Authorization: Bearer <TOKEN_ADMIN>


Réponse attendue (200 OK) :

{
  "id": 1,
  "fullName": "John Doe Updated",
  "email": "johnupdated@example.com",
  "user": {
    "id": 1,
    "username": "john_doe",
    "role": "ETUDIANT"
  }
}

5️⃣ Supprimer un étudiant (DELETE /api/students/{id})

URL :

DELETE http://localhost:8080/api/students/1


Headers :

Authorization: Bearer <TOKEN_ADMIN>


Réponse attendue :

204 No Content

🔹 Notes importantes pour Postman

Pour tous les endpoints Admin, tu dois fournir un JWT valide dans le header Authorization: Bearer <TOKEN>.

Si tu veux tester /students/me, connecte-toi en tant qu’étudiant et utilise son token.

Pour créer un étudiant, assure-toi que le username n’existe pas déjà (unique).

Pour supprimer un étudiant, vérifie qu’aucune foreign key ne bloque la suppression (ex. notifications, profils).

Si tu veux, je peux te préparer un fichier Postman complet (.json) prêt à importer avec tous ces endpoints et des exemples de corps pour tester directement.

Veux‑tu que je fasse ça ?