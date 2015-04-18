-- MySQL dump 10.13  Distrib 5.6.19, for Win64 (x86_64)
--
-- Host: localhost    Database: dsmpubsearch
-- ------------------------------------------------------
-- Server version	5.6.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `advdisease`
--

DROP TABLE IF EXISTS `advdisease`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advdisease` (
  `idadvDisease` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `value` varchar(512) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idadvDisease`),
  UNIQUE KEY `idadvDisease_UNIQUE` (`idadvDisease`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Advanced Search by Disease selection';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advdisease`
--

LOCK TABLES `advdisease` WRITE;
/*!40000 ALTER TABLE `advdisease` DISABLE KEYS */;
/*!40000 ALTER TABLE `advdisease` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `advdrugs`
--

DROP TABLE IF EXISTS `advdrugs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advdrugs` (
  `idadvDrugs` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `value` varchar(512) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idadvDrugs`),
  UNIQUE KEY `idadvDrugs_UNIQUE` (`idadvDrugs`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Advanced Search by Drugs selection';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advdrugs`
--

LOCK TABLES `advdrugs` WRITE;
/*!40000 ALTER TABLE `advdrugs` DISABLE KEYS */;
/*!40000 ALTER TABLE `advdrugs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `advpublications`
--

DROP TABLE IF EXISTS `advpublications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advpublications` (
  `idadvPublications` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `value` varchar(512) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idadvPublications`),
  UNIQUE KEY `idadvPublication_UNIQUE` (`idadvPublications`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Advanced Search by Publication selection';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advpublications`
--

LOCK TABLES `advpublications` WRITE;
/*!40000 ALTER TABLE `advpublications` DISABLE KEYS */;
/*!40000 ALTER TABLE `advpublications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `advspecialty`
--

DROP TABLE IF EXISTS `advspecialty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advspecialty` (
  `idadvSpecialty` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `value` varchar(512) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idadvSpecialty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Advanced Search Specialty selection';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advspecialty`
--

LOCK TABLES `advspecialty` WRITE;
/*!40000 ALTER TABLE `advspecialty` DISABLE KEYS */;
/*!40000 ALTER TABLE `advspecialty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `advtoxicology`
--

DROP TABLE IF EXISTS `advtoxicology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advtoxicology` (
  `idadvToxicology` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `value` varchar(512) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idadvToxicology`),
  UNIQUE KEY `idadvToxicology_UNIQUE` (`idadvToxicology`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Advanced Search by Toxicology selection';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advtoxicology`
--

LOCK TABLES `advtoxicology` WRITE;
/*!40000 ALTER TABLE `advtoxicology` DISABLE KEYS */;
/*!40000 ALTER TABLE `advtoxicology` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `articles`
--

DROP TABLE IF EXISTS `articles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `articles` (
  `idarticles` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) DEFAULT NULL,
  `emailto` varchar(128) DEFAULT NULL,
  `pubmedid` varchar(45) DEFAULT NULL,
  `issn` varchar(45) DEFAULT NULL,
  `journal` varchar(512) DEFAULT NULL,
  `articletitle` varchar(512) DEFAULT NULL,
  `articlecopyrite` varchar(512) DEFAULT NULL,
  `abstract` varchar(8192) DEFAULT NULL,
  `pubyy` varchar(16) DEFAULT NULL,
  `pubMM` varchar(16) DEFAULT NULL,
  `pubdd` varchar(16) DEFAULT NULL,
  `createdate` timestamp NULL DEFAULT NULL,
  `emaildate` timestamp NULL DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `publish` int(11) DEFAULT NULL,
  PRIMARY KEY (`idarticles`),
  UNIQUE KEY `idarticles_UNIQUE` (`idarticles`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articles`
--

LOCK TABLES `articles` WRITE;
/*!40000 ALTER TABLE `articles` DISABLE KEYS */;
/*!40000 ALTER TABLE `articles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basesearch`
--

DROP TABLE IF EXISTS `basesearch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `basesearch` (
  `idbasesearch` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `specialty` varchar(512) DEFAULT NULL,
  `terms` varchar(2048) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idbasesearch`),
  UNIQUE KEY `idbasesearch_UNIQUE` (`idbasesearch`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='derived from Basic Search widget - Specialty dropdown and open-neded text ''terms'' input: serach = [specialty] AND [Terms]';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basesearch`
--

LOCK TABLES `basesearch` WRITE;
/*!40000 ALTER TABLE `basesearch` DISABLE KEYS */;
/*!40000 ALTER TABLE `basesearch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `searchqueries`
--

DROP TABLE IF EXISTS `searchqueries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `searchqueries` (
  `idsearchqueries` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) DEFAULT NULL,
  `query` varchar(4092) DEFAULT NULL,
  `createdate` timestamp NULL DEFAULT NULL,
  `active` int(11) DEFAULT NULL COMMENT 'true if in use, false if NOT in use',
  PRIMARY KEY (`idsearchqueries`),
  UNIQUE KEY `idsearchterms_UNIQUE` (`idsearchqueries`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `searchqueries`
--

LOCK TABLES `searchqueries` WRITE;
/*!40000 ALTER TABLE `searchqueries` DISABLE KEYS */;
/*!40000 ALTER TABLE `searchqueries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `searchresults`
--

DROP TABLE IF EXISTS `searchresults`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `searchresults` (
  `idsearchresults` int(11) NOT NULL AUTO_INCREMENT,
  `idusers` int(11) DEFAULT NULL,
  `uid` int(11) DEFAULT NULL,
  PRIMARY KEY (`idsearchresults`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `searchresults`
--

LOCK TABLES `searchresults` WRITE;
/*!40000 ALTER TABLE `searchresults` DISABLE KEYS */;
/*!40000 ALTER TABLE `searchresults` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `idusers` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(45) DEFAULT NULL,
  `password` varchar(45) DEFAULT NULL,
  `sms` varchar(45) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `createdate` datetime DEFAULT NULL,
  PRIMARY KEY (`idusers`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-26 17:33:18
