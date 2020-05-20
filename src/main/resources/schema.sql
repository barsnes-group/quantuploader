/*
THIS FILE CONTAINS SQL SENTENCES INTENDED TO CREATE TABLES ON THE DB, BUT IT 
RUNS AT THE SAME TIME THAN THE AUTOMATIC SCHEMA GENERATION, SO
THIS MAY CONFLICT WITH spring.jpa.hibernate.ddl-auto OR eclipselink.ddl-generation
CONFIGURATION; SO, IT'S USUALLY RECOMMENDED NOT TO USE THEM BOTH AT THE SAME TIME. 
BUT, IF WE KNOW WHAT WE ARE DOING (IE: LIKE CREATING JUST VIEWS, ALTHOUGH I'VE CHECKED IT DOES NOT WORK)
THERE SHOULD NOT BE ANY PROBLEM AT ALL
*/

-- CREATION OF THE SCHEMA ITSELF CANNOT BE DONE AUTOMATICALLY! 
-- IT MUST BE CREATED MANUALLY BEFORE RUNNING THE APP WITH THIS COMMAND:
-- CREATE SCHEMA IF NOT EXISTS `QuantUploaderCSFPR` DEFAULT CHARACTER SET utf8 ;


CREATE TABLE IF NOT EXISTS `disease_groups` (
  `min` varchar(100) NOT NULL DEFAULT ' ',
  `full` varchar(500) NOT NULL DEFAULT ' ',
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `min_UNIQUE` (`min`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `diseases` (
  `min` varchar(100) NOT NULL DEFAULT ' ',
  `full` varchar(500) NOT NULL DEFAULT ' ',
  `id` int NOT NULL AUTO_INCREMENT,
  `disease_group` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (disease_group) REFERENCES disease_groups(id),
  UNIQUE KEY `min_UNIQUE` (`min`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
