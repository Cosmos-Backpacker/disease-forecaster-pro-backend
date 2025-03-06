/*
SQLyog Community v13.2.1 (64 bit)
MySQL - 8.0.34 : Database - forecasterpro
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`forecasterpro` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `forecasterpro`;

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(256) DEFAULT NULL,
  `userAccount` varchar(256) DEFAULT NULL,
  `avatarUrl` varchar(1024) DEFAULT NULL,
  `gender` tinyint DEFAULT NULL,
  `userPassword` varchar(512) NOT NULL,
  `phone` varchar(128) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  `userStatus` int NOT NULL DEFAULT '0',
  `createTime` datetime DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `isDelete` tinyint DEFAULT '0',
  `role` int NOT NULL DEFAULT '0' COMMENT '用户角色 0-普通用户，1-管理员',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `user` */

insert  into `user`(`id`,`username`,`userAccount`,`avatarUrl`,`gender`,`userPassword`,`phone`,`email`,`userStatus`,`createTime`,`updateTime`,`isDelete`,`role`) values 
(1,NULL,'admin',NULL,NULL,'711a6bc7e0250bf305feba22a37165e5',NULL,NULL,0,NULL,NULL,0,0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
