CREATE DATABASE IF NOT EXISTS airline;
USE airline;

CREATE TABLE IF NOT EXISTS passengers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    seat_number INT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS seats (
    seat_id INT PRIMARY KEY,
    passenger_id INT DEFAULT NULL
);

-- Bulk insert 100 passengers
INSERT INTO passengers (name) VALUES 
('Passenger 1'), ('Passenger 2'), ('Passenger 3'), ('Passenger 4'), ('Passenger 5'),
('Passenger 6'), ('Passenger 7'), ('Passenger 8'), ('Passenger 9'), ('Passenger 10'),
('Passenger 11'), ('Passenger 12'), ('Passenger 13'), ('Passenger 14'), ('Passenger 15'),
('Passenger 16'), ('Passenger 17'), ('Passenger 18'), ('Passenger 19'), ('Passenger 20'),
('Passenger 21'), ('Passenger 22'), ('Passenger 23'), ('Passenger 24'), ('Passenger 25'),
('Passenger 26'), ('Passenger 27'), ('Passenger 28'), ('Passenger 29'), ('Passenger 30'),
('Passenger 31'), ('Passenger 32'), ('Passenger 33'), ('Passenger 34'), ('Passenger 35'),
('Passenger 36'), ('Passenger 37'), ('Passenger 38'), ('Passenger 39'), ('Passenger 40'),
('Passenger 41'), ('Passenger 42'), ('Passenger 43'), ('Passenger 44'), ('Passenger 45'),
('Passenger 46'), ('Passenger 47'), ('Passenger 48'), ('Passenger 49'), ('Passenger 50'),
('Passenger 51'), ('Passenger 52'), ('Passenger 53'), ('Passenger 54'), ('Passenger 55'),
('Passenger 56'), ('Passenger 57'), ('Passenger 58'), ('Passenger 59'), ('Passenger 60'),
('Passenger 61'), ('Passenger 62'), ('Passenger 63'), ('Passenger 64'), ('Passenger 65'),
('Passenger 66'), ('Passenger 67'), ('Passenger 68'), ('Passenger 69'), ('Passenger 70'),
('Passenger 71'), ('Passenger 72'), ('Passenger 73'), ('Passenger 74'), ('Passenger 75'),
('Passenger 76'), ('Passenger 77'), ('Passenger 78'), ('Passenger 79'), ('Passenger 80'),
('Passenger 81'), ('Passenger 82'), ('Passenger 83'), ('Passenger 84'), ('Passenger 85'),
('Passenger 86'), ('Passenger 87'), ('Passenger 88'), ('Passenger 89'), ('Passenger 90'),
('Passenger 91'), ('Passenger 92'), ('Passenger 93'), ('Passenger 94'), ('Passenger 95'),
('Passenger 96'), ('Passenger 97'), ('Passenger 98'), ('Passenger 99'), ('Passenger 100');

-- Bulk insert 100 seats (using a single multi-row INSERT for speed)
INSERT INTO seats (seat_id) VALUES 
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),
(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),
(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),
(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),(91),(92),(93),(94),(95),(96),(97),(98),(99),(100);
