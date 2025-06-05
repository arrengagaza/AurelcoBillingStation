-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 02, 2025 at 05:24 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `aurelco`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`id`, `email`, `password`, `created_at`) VALUES
(1, 'admin_aurelco@gmail.com', '$2a$10$ECxGFn6/PtsGCMSbJbXavuZAJUDO0nDsNwtNFzVVXg.AVsTrb5Fam', '2025-05-31 12:39:13');

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `id` int(11) NOT NULL,
  `consumer_id` int(11) NOT NULL,
  `meter_id` int(11) NOT NULL,
  `billing_date` date NOT NULL,
  `due_date` date NOT NULL,
  `previous_reading` decimal(10,2) NOT NULL,
  `current_reading` decimal(10,2) NOT NULL,
  `consumption` decimal(10,2) NOT NULL,
  `rate` decimal(10,2) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `status` enum('pending','paid','overdue','disputed') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`id`, `consumer_id`, `meter_id`, `billing_date`, `due_date`, `previous_reading`, `current_reading`, `consumption`, `rate`, `amount`, `status`, `created_at`) VALUES
(7, 2, 4, '2025-06-02', '2025-06-17', 0.00, 200.00, 200.00, 12.00, 2400.00, 'paid', '2025-06-02 01:49:47');

-- --------------------------------------------------------

--
-- Table structure for table `consumers`
--

CREATE TABLE `consumers` (
  `id` int(11) NOT NULL,
  `given_name` varchar(50) NOT NULL,
  `middle_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `mobile` varchar(15) NOT NULL,
  `address` varchar(200) NOT NULL,
  `account_no` varchar(12) NOT NULL,
  `meter_number` varchar(20) NOT NULL,
  `birthday` date NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('Active','Inactive','Disconnected') NOT NULL DEFAULT 'Active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `consumers`
--

INSERT INTO `consumers` (`id`, `given_name`, `middle_name`, `last_name`, `email`, `mobile`, `address`, `account_no`, `meter_number`, `birthday`, `password`, `created_at`, `status`) VALUES
(2, 'Test', 'Account1', 'Testing', 'account@gmail.com', '09000000001', 'Purok 4, Brgy. Pingit, Baler Aurora', '20250002', '0000000002', '1960-06-04', '$2a$10$IFVwIGQzmLpg.YVPj.zHiOGZvnrHyo4FaWKn8ZHAKqpKGhwZ2jGo.', '2025-06-01 07:38:04', 'Active'),
(5, 'Account', 'Testing3', 'Another', 'testing3@gmail.com', '09090909090', 'Suclayin, Baler Aurora', '0000000003', '123456', '1999-05-23', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', '2025-06-02 02:39:47', 'Active'),
(6, 'Juan', 'Cruz', 'Dela Cruz', 'juan@gmail.com', '0999999999', 'Purok 4, Brgy. 3, Baler, Aurora', '233312333', '111222333', '1962-06-01', '$2a$10$77nRoKNFfYjwH9Yu8NCk0.rDYFKrNmKSSdnhFerAk3S26kS3.pd8u', '2025-06-02 03:03:00', 'Active'),
(7, 'Juana May', 'Testing', 'Cruz', 'juana@gmail.com', '09999999999', 'Purok 2, Brgy. 4, Baler Aurora', '33333', '11111', '1960-04-01', '$2a$10$SRZjdRjFRDOal0IT96Q6r.iRSKO.EhJPqQGTxO596wUVwAcJzfiGy', '2025-06-02 03:23:27', 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `disconnections`
--

CREATE TABLE `disconnections` (
  `id` int(11) NOT NULL,
  `consumer_id` int(11) NOT NULL,
  `bill_id` int(11) NOT NULL,
  `notice_date` date NOT NULL,
  `disconnection_date` date DEFAULT NULL,
  `reconnection_date` date DEFAULT NULL,
  `reason` text DEFAULT NULL,
  `status` enum('notice_sent','disconnected','reconnected') DEFAULT 'notice_sent',
  `processed_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `meters`
--

CREATE TABLE `meters` (
  `id` int(11) NOT NULL,
  `consumer_id` int(11) NOT NULL,
  `meter_number` varchar(20) NOT NULL,
  `installation_date` date NOT NULL,
  `status` enum('active','inactive','defective') DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meters`
--

INSERT INTO `meters` (`id`, `consumer_id`, `meter_number`, `installation_date`, `status`) VALUES
(4, 2, '0000000002', '2025-06-02', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `bill_id` int(11) NOT NULL,
  `consumer_id` int(11) NOT NULL,
  `or_number` varchar(20) NOT NULL,
  `payment_date` date NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `payment_method` enum('cash','gcash','bank_transfer','credit_card') NOT NULL,
  `received_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`id`, `bill_id`, `consumer_id`, `or_number`, `payment_date`, `amount`, `payment_method`, `received_by`, `created_at`) VALUES
(4, 7, 2, '2025003', '2025-06-02', 2400.00, 'cash', 1, '2025-06-02 01:50:34');

-- --------------------------------------------------------

--
-- Table structure for table `rates`
--

CREATE TABLE `rates` (
  `id` int(11) NOT NULL,
  `rate_name` varchar(50) NOT NULL,
  `rate_per_kwh` decimal(10,4) NOT NULL,
  `effective_date` date NOT NULL,
  `created_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`id`),
  ADD KEY `consumer_id` (`consumer_id`),
  ADD KEY `meter_id` (`meter_id`);

--
-- Indexes for table `consumers`
--
ALTER TABLE `consumers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `account_no` (`account_no`),
  ADD UNIQUE KEY `uk_consumers_meter` (`meter_number`);

--
-- Indexes for table `disconnections`
--
ALTER TABLE `disconnections`
  ADD PRIMARY KEY (`id`),
  ADD KEY `consumer_id` (`consumer_id`),
  ADD KEY `bill_id` (`bill_id`),
  ADD KEY `processed_by` (`processed_by`);

--
-- Indexes for table `meters`
--
ALTER TABLE `meters`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `meter_number` (`meter_number`),
  ADD KEY `consumer_id` (`consumer_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `or_number` (`or_number`),
  ADD KEY `bill_id` (`bill_id`),
  ADD KEY `received_by` (`received_by`),
  ADD KEY `consumer_id` (`consumer_id`);

--
-- Indexes for table `rates`
--
ALTER TABLE `rates`
  ADD PRIMARY KEY (`id`),
  ADD KEY `created_by` (`created_by`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `bills`
--
ALTER TABLE `bills`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `consumers`
--
ALTER TABLE `consumers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `disconnections`
--
ALTER TABLE `disconnections`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `meters`
--
ALTER TABLE `meters`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `rates`
--
ALTER TABLE `rates`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bills`
--
ALTER TABLE `bills`
  ADD CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`consumer_id`) REFERENCES `consumers` (`id`),
  ADD CONSTRAINT `bills_ibfk_2` FOREIGN KEY (`meter_id`) REFERENCES `meters` (`id`);

--
-- Constraints for table `disconnections`
--
ALTER TABLE `disconnections`
  ADD CONSTRAINT `disconnections_ibfk_1` FOREIGN KEY (`consumer_id`) REFERENCES `consumers` (`id`),
  ADD CONSTRAINT `disconnections_ibfk_2` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`id`),
  ADD CONSTRAINT `disconnections_ibfk_3` FOREIGN KEY (`processed_by`) REFERENCES `admins` (`id`);

--
-- Constraints for table `meters`
--
ALTER TABLE `meters`
  ADD CONSTRAINT `meters_ibfk_1` FOREIGN KEY (`consumer_id`) REFERENCES `consumers` (`id`);

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`id`),
  ADD CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`received_by`) REFERENCES `admins` (`id`),
  ADD CONSTRAINT `payments_ibfk_3` FOREIGN KEY (`consumer_id`) REFERENCES `consumers` (`id`);

--
-- Constraints for table `rates`
--
ALTER TABLE `rates`
  ADD CONSTRAINT `rates_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `admins` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
