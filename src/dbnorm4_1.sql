-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 19, 2026 at 10:48 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `dbnorm4`
--

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `item_id` int(5) NOT NULL,
  `owner_id` int(10) NOT NULL,
  `item_name` varchar(150) NOT NULL,
  `item_quantity` int(3) DEFAULT 1,
  `description` text DEFAULT NULL,
  `item_image` varchar(255) NOT NULL DEFAULT '/resources/defaultpictures/axolotl.jpg',
  `category` enum('Textbooks','Electronics','Equipment','Supplies','Consumable_Goods','Other') NOT NULL,
  `condition` enum('Fair','Good','New') NOT NULL,
  `price` int(6) DEFAULT NULL,
  `availability_status` enum('Available','Unavailable') DEFAULT 'Available',
  `maximum_borrow_days` int(2) DEFAULT NULL,
  `desired_item` varchar(255) DEFAULT NULL,
  `date_listed` timestamp NOT NULL DEFAULT current_timestamp(),
  `pickup_area` varchar(255) DEFAULT NULL,
  `pickup_time` varchar(10) DEFAULT NULL,
  `pickup_days` set('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday') DEFAULT NULL,
  `action` enum('Marketplace','Marketplace_Withdrawn','Marketplace_Completed','Sharing','Sharing_Withdrawn','Sharing_Approval','Sharing_Return','Sharing_Completed','Trade','Trade_Withdrawn','Trade_Approval','Trade_Completed') NOT NULL,
  `items_is_archived` tinyint(1) DEFAULT 0,
  `items_archived_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`item_id`, `owner_id`, `item_name`, `item_quantity`, `description`, `item_image`, `category`, `condition`, `price`, `availability_status`, `maximum_borrow_days`, `desired_item`, `date_listed`, `pickup_area`, `pickup_time`, `pickup_days`, `action`, `items_is_archived`, `items_archived_at`) VALUES
(1, 1, 'Java Book', 10, 'Programming reference', '/resources/items/java_book.jpg', 'Textbooks', 'Good', 250, 'Available', NULL, NULL, '2026-05-17 22:04:03', 'HPSB 401', '10 AM', 'Monday', 'Marketplace', 0, NULL),
(2, 2, 'Calculator', 9, 'Casio fx-991EX', '/resources/items/calculator.jpg', 'Electronics', 'Good', 800, 'Available', NULL, NULL, '2026-05-17 22:04:03', 'HPSB 402', '1 PM', 'Tuesday', 'Marketplace', 0, NULL),
(3, 3, 'Finance Vol 1', 6, 'Accounting book', '/resources/items/books.jpg', 'Textbooks', 'Fair', 150, 'Available', NULL, NULL, '2026-05-17 22:04:03', 'HPSB 501', '3 PM', 'Wednesday', 'Marketplace', 0, NULL),
(4, 4, 'ID Lanyard', 20, 'UMak design strap', '/resources/items/lanyard.jpg', 'Other', 'New', 50, 'Available', NULL, NULL, '2026-05-17 22:04:03', 'HPSB 502', '2 PM', 'Thursday', 'Marketplace', 0, NULL),
(5, 5, 'Cutter', 10, 'a hand tool with a sharp, replaceable blade used for precise slicing and trimming.', '/resources/items/cutter.jpg', 'Equipment', 'New', NULL, 'Available', 5, NULL, '2026-05-17 22:04:03', 'HPSB 503', '9 AM', 'Monday', 'Sharing', 0, NULL),
(6, 6, 'Mini fan', 11, 'a small, portable device used for personal cooling or localized ventilation.', '/resources/items/mini_fan.jpg', 'Equipment', 'Good', NULL, 'Available', 7, NULL, '2026-05-17 22:04:03', 'HPSB 504', '4 PM', 'Friday', 'Sharing', 0, NULL),
(7, 7, 'Multimeter', 7, 'handheld tool used to test and measure electrical circuits.', '/resources/items/multimeter.jpg', 'Equipment', 'New', NULL, 'Available', NULL, 'Medical Tape', '2026-05-17 22:04:03', 'HPSB 505', '11 AM', 'Wednesday', 'Trade', 0, NULL),
(8, 8, 'Colored Paper', 30, 'tinted paper used for writing, printing, crafting, and organizing.', '/resources/items/colored_paper.jpg', 'Supplies', 'Good', 300, 'Available', NULL, NULL, '2026-05-17 22:04:03', 'HPSB 506', '4 PM', 'Thursday', 'Marketplace', 0, NULL),
(9, 9, 'Books ', 17, 'a bound collections of pages used to record, store, and share information, stories, or art.', '/resources/items/books.jpg', 'Textbooks', 'Good', 500, 'Available', NULL, NULL, '2026-05-17 22:04:03', 'HPSB 507', '6 PM', 'Friday', 'Marketplace', 0, NULL),
(10, 10, 'Notebook', 19, 'filled with blank, ruled, or gridded pages used for writing, drawing, and organizing information.', '/resources/items/notebook.jpg', 'Textbooks', 'Fair', NULL, 'Available', NULL, 'Notebook', '2026-05-17 22:04:03', 'HPSB 508', '11 AM', 'Tuesday', 'Trade', 0, NULL),
(11, 1, 'USB Flash Drive 64GB', 15, 'High-speed storage', '/resources/items/usb.jpg', 'Electronics', 'Good', 300, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 607', '9 AM', 'Monday', 'Marketplace_Completed', 0, NULL),
(12, 2, 'Bond Paper', 100, 'A high-quality, durable writing and printing paper.', '/resources/items/bond_paper.jpg', 'Supplies', 'Good', 90, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 608', '10 AM', 'Tuesday', 'Marketplace_Completed', 0, NULL),
(13, 3, 'HDMI', 15, 'A standard cable and port used to transmit high-quality digital video and audio from one device to another through a single connection.', '/resources/items/hdmi.jpg', 'Electronics', 'Good', 250, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 609', '11 AM', 'Wednesday', 'Marketplace_Completed', 0, NULL),
(14, 4, 'Supplies Set', 10, 'Geometry tools', '/resources/items/pencil_pen_and_a_ruler.jpg', 'Supplies', 'New', 70, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 610', '1 PM', 'Thursday', 'Marketplace_Completed', 0, NULL),
(15, 5, 'Correction Pen ', 29, 'A handheld tool that dispenses white fluid to cover up handwriting or printing errors so you can write over them.', '/resources/items/correction_pen.jpg', 'Supplies', 'Good', 60, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 611', '2 PM', 'Friday', 'Marketplace_Completed', 0, NULL),
(16, 6, 'Lab Coat Medium', 25, 'Science lab coat', '/resources/items/labcoat.jpg', 'Equipment', 'Good', NULL, 'Available', 5, NULL, '2026-05-17 22:10:00', 'HPSB 612', '9 AM', 'Monday', 'Sharing', 0, NULL),
(17, 7, 'Remote', 20, 'A wireless handheld device used to operate electronic equipment from a distance.', '/resources/items/remote.jpg', 'Electronics', 'Good', NULL, 'Available', 7, NULL, '2026-05-17 22:10:00', 'HPSB 613', '10 AM', 'Tuesday', 'Sharing', 0, NULL),
(18, 8, 'Extension Cord', 20, 'Heavy-duty extension', '/resources/items/extension_cord.jpg', 'Electronics', 'Fair', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 614', '11 AM', 'Wednesday', 'Sharing', 0, NULL),
(19, 9, 'Glue Gun ', 16, 'Industrial glue tool', '/resources/items/glue_gun.jpg', 'Equipment', 'Good', NULL, 'Available', 3, NULL, '2026-05-17 22:10:00', 'HPSB 615', '1 PM', 'Thursday', 'Sharing', 0, NULL),
(20, 10, 'Bluetooth Speaker', 30, 'Portable audio device', '/resources/items/bluetooth_speaker.jpg', 'Electronics', 'Good', NULL, 'Available', 7, NULL, '2026-05-17 22:10:00', 'HPSB 617', '2 PM', 'Friday', 'Sharing', 0, NULL),
(21, 1, 'Digital Camera', 10, 'Entry-level DSLR', '/resources/items/camera.jpg', 'Electronics', 'Good', NULL, 'Available', 5, NULL, '2026-05-17 22:10:00', 'HPSB 701', '9 AM', 'Monday', 'Sharing_Withdrawn', 0, NULL),
(22, 2, 'Scissors Heavy Duty', 18, 'Industrial scissors', '/resources/items/scissors.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 702', '10 AM', 'Tuesday', 'Sharing_Withdrawn', 0, NULL),
(23, 3, 'Large Stapler', 30, 'Office stapler', '/resources/items/stapler.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 703', '11 AM', 'Wednesday', 'Sharing_Withdrawn', 0, NULL),
(24, 4, 'Printer Inks', 150, 'HP ink refill', '/resources/items/printer_ink.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 704', '1 PM', 'Thursday', 'Sharing_Withdrawn', 0, NULL),
(25, 5, 'Cleaning Kit', 40, 'Electronics cleaning set', '/resources/items/clean_toolkit.jpg', 'Supplies', 'New', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 705', '2 PM', 'Friday', 'Sharing_Withdrawn', 0, NULL),
(26, 6, 'Laptop Stand', 1, 'Aluminum stand', '/resources/items/laptop.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 706', '9 AM', 'Monday', 'Sharing_Approval', 0, NULL),
(27, 7, 'Notebook Bundle', 1, '5-subject notebook set', '/resources/items/notebook.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 707', '10 AM', 'Tuesday', 'Sharing_Approval', 0, NULL),
(28, 8, 'Powerbank 20000mAh', 1, 'Fast charging', '/resources/items/powerbank.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 708', '11 AM', 'Wednesday', 'Sharing_Approval', 0, NULL),
(29, 9, 'Mini Fan USB', 1, 'Portable fan', '/resources/items/mini_fan.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 709', '1 PM', 'Thursday', 'Sharing_Approval', 0, NULL),
(30, 10, 'Calculator Spare', 1, 'Backup calculator', '/resources/items/calculator.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 710', '2 PM', 'Friday', 'Sharing_Approval', 0, NULL),
(31, 1, 'Marker Pack', 1, 'Whiteboard markers', '/resources/items/correction_pen.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 712', '9 AM', 'Monday', 'Sharing_Return', 0, NULL),
(32, 2, 'Tape Set', 1, 'Masking + Scotch', '/resources/items/scotch_tape.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 801', '10 AM', 'Tuesday', 'Sharing_Return', 0, NULL),
(33, 3, 'Scotch Dispenser', 1, 'Tape holder', '/resources/items/scotch_type_dispenser.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 802', '11 AM', 'Wednesday', 'Sharing_Return', 0, NULL),
(34, 4, 'Glue Stick Set', 1, 'Pack of glue sticks', '/resources/items/glue_stick.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 803', '1 PM', 'Thursday', 'Sharing_Return', 0, NULL),
(35, 5, 'Staple Pack', 1, 'Stapler refill wires', '/resources/items/staples.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 804', '2 PM', 'Friday', 'Sharing_Return', 0, NULL),
(36, 6, 'Mechanical Keyboard', 1, 'RGB keyboard', '/resources/items/keyboard.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 805', '9 AM', 'Monday', 'Sharing_Completed', 0, NULL),
(37, 7, 'Mic Set', 1, 'USB microphone', '/resources/items/mic.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 806', '10 AM', 'Tuesday', 'Sharing_Completed', 0, NULL),
(38, 8, 'Stylus Pen', 1, 'Tablet stylus', '/resources/items/stylus.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 807', '11 AM', 'Wednesday', 'Sharing_Completed', 0, NULL),
(39, 9, 'Memory Card 64GB', 1, 'SD card storage', '/resources/items/camera memory card.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 808', '1 PM', 'Thursday', 'Sharing_Completed', 0, NULL),
(40, 10, 'Backpack', 1, 'School bag', '/resources/items/bagpack.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 809', '2 PM', 'Friday', 'Sharing_Completed', 0, NULL),
(41, 1, 'Laptop i5', 1, 'Student laptop', '/resources/items/laptop.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Mouse', '2026-05-17 22:10:00', 'HPSB 810', '9 AM', 'Monday', 'Trade', 0, NULL),
(42, 2, 'Android Phone', 1, 'Mid-range phone', '/resources/items/phone.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Powerbank', '2026-05-17 22:10:00', 'HPSB 811', '10 AM', 'Tuesday', 'Trade', 0, NULL),
(43, 3, 'USB Drive 16GB', 1, 'Basic storage', '/resources/items/usb.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Notebook', '2026-05-17 22:10:00', 'HPSB 812', '11 AM', 'Wednesday', 'Trade', 0, NULL),
(44, 4, 'Wireless Mouse', 1, 'Bluetooth mouse', '/resources/items/remote.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'USB Cable', '2026-05-17 22:10:00', 'HPSB 813', '1 PM', 'Thursday', 'Trade', 0, NULL),
(45, 5, 'HD Webcam', 1, '1080p camera', '/resources/items/camera.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Headset', '2026-05-17 22:10:00', 'HPSB 814', '2 PM', 'Friday', 'Trade', 0, NULL),
(46, 6, 'Lab Microscope Slides', 1, 'Biology lab materials', '/resources/items/microscope.jpg', 'Equipment', 'Good', NULL, 'Available', 7, NULL, '2026-05-17 22:10:00', 'HPSB 815', '9 AM', 'Monday', 'Sharing_Approval', 0, NULL),
(47, 7, 'HDMI Cable Pro', 1, '4K HDMI cable', '/resources/items/hdmi.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 817', '10 AM', 'Tuesday', 'Sharing_Approval', 0, NULL),
(48, 8, 'Notebook Bundle', 1, 'Academic notebooks set', '/resources/items/notebook.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 901', '11 AM', 'Wednesday', 'Sharing_Approval', 0, NULL),
(49, 9, 'Wireless Mouse', 1, 'Bluetooth mouse', '/resources/items/remote.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 902', '1 PM', 'Thursday', 'Sharing_Approval', 0, NULL),
(50, 10, 'Calculator FX Model', 1, 'Advanced calculator', '/resources/items/calculator.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 906', '2 PM', 'Friday', 'Sharing_Approval', 0, NULL),
(51, 1, 'Marker Set', 1, 'Whiteboard markers', '/resources/items/correction_pen.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 907', '9 AM', 'Monday', 'Sharing_Return', 0, NULL),
(52, 2, 'Tape Bundle', 1, 'Masking + Scotch tape', '/resources/items/scotch_tape.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 908', '10 AM', 'Tuesday', 'Sharing_Return', 0, NULL),
(53, 3, 'Glue Stick Pack', 1, 'School glue sticks', '/resources/items/glue_stick.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 909', '11 AM', 'Wednesday', 'Sharing_Return', 0, NULL),
(54, 4, 'Staple Pack', 1, 'Stapler refill wires', '/resources/items/staples.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1001', '1 PM', 'Thursday', 'Sharing_Return', 0, NULL),
(55, 5, 'Scotch Dispenser', 1, 'Tape holder tool', '/resources/items/scotch_type_dispenser.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1002', '2 PM', 'Friday', 'Sharing_Return', 0, NULL),
(56, 6, 'Mechanical Keyboard RGB', 1, 'Gaming keyboard', '/resources/items/keyboard.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1003', '9 AM', 'Monday', 'Sharing_Completed', 0, NULL),
(57, 7, 'USB Microphone', 1, 'Podcast mic', '/resources/items/mic.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1004', '10 AM', 'Tuesday', 'Sharing_Completed', 0, NULL),
(58, 8, 'Tablet Stylus', 1, 'Digital pen', '/resources/items/stylus.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1005', '11 AM', 'Wednesday', 'Sharing_Completed', 0, NULL),
(59, 9, 'SD Card 64GB', 1, 'Memory storage', '/resources/items/camera memory card.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1006', '1 PM', 'Thursday', 'Sharing_Completed', 0, NULL),
(60, 10, 'School Backpack', 1, 'Student bag', '/resources/items/bagpack.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1007', '2 PM', 'Friday', 'Sharing_Completed', 0, NULL),
(61, 1, 'Laptop i5 Gen', 1, 'Student laptop', '/resources/items/laptop.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Mouse', '2026-05-17 22:10:00', 'HPSB 1008', '9 AM', 'Monday', 'Trade', 0, NULL),
(62, 2, 'Android Phone', 1, 'Midrange smartphone', '/resources/items/phone.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Powerbank', '2026-05-17 22:10:00', 'HPSB 1009', '10 AM', 'Tuesday', 'Trade', 0, NULL),
(63, 3, 'USB Drive 32GB', 1, 'Flash storage', '/resources/items/usb.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Notebook', '2026-05-17 22:10:00', 'HPSB 1010', '11 AM', 'Wednesday', 'Trade', 0, NULL),
(64, 4, 'Wireless Mouse', 1, 'Bluetooth mouse', '/resources/items/remote.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'USB Cable', '2026-05-17 22:10:00', 'HPSB 1011', '1 PM', 'Thursday', 'Trade', 0, NULL),
(65, 5, 'HD Webcam', 1, 'HD 1080p camera', '/resources/items/camera.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, 'Headset', '2026-05-17 22:10:00', 'HPSB 1012', '2 PM', 'Friday', 'Trade', 0, NULL),
(66, 6, 'HDMI Cable', 1, '4K cable', '/resources/items/hdmi.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1013', '9 AM', 'Monday', 'Trade_Withdrawn', 0, NULL),
(67, 7, 'Notebook Set', 1, 'Hardbound notebooks', '/resources/items/notebook.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1014', '10 AM', 'Tuesday', 'Trade_Withdrawn', 0, NULL),
(68, 8, 'Calculator FX', 1, 'Scientific calculator', '/resources/items/calculator.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1015', '11 AM', 'Wednesday', 'Trade_Withdrawn', 0, NULL),
(69, 9, 'Printer Cable', 1, 'USB cable', '/resources/items/extension_cord.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1016', '1 PM', 'Thursday', 'Trade_Withdrawn', 0, NULL),
(70, 10, 'School Folder Set', 1, 'Document organizers', '/resources/items/envelope.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1017', '2 PM', 'Friday', 'Trade_Withdrawn', 0, NULL),
(71, 1, 'USB-C Hub', 1, 'Multiport adapter', '/resources/items/usb.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1101', '9 AM', 'Monday', 'Trade_Approval', 0, NULL),
(72, 2, 'Laptop Cooling Pad', 1, 'Cooling stand', '/resources/items/laptop.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1102', '10 AM', 'Tuesday', 'Trade_Approval', 0, NULL),
(73, 3, 'Powerbank 10k', 1, 'Fast charging', '/resources/items/powerbank.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1103', '11 AM', 'Wednesday', 'Trade_Approval', 0, NULL),
(74, 4, 'Mechanical Mouse', 1, 'Gaming mouse', '/resources/items/remote.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1104', '1 PM', 'Thursday', 'Trade_Approval', 0, NULL),
(75, 5, 'HD Camera Lens', 1, 'DSLR accessory', '/resources/items/camera.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1201', '2 PM', 'Friday', 'Trade_Approval', 0, NULL),
(76, 6, 'HDMI Splitter', 1, 'Video splitter', '/resources/items/hdmi.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 1202', '9 AM', 'Monday', 'Trade_Completed', 0, NULL),
(77, 7, 'Notebook Premium', 1, 'Hardbound set', '/resources/items/notebook.jpg', 'Supplies', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 401', '10 AM', 'Tuesday', 'Trade_Completed', 0, NULL),
(78, 8, 'Calculator Advanced', 1, 'Engineering calc', '/resources/items/calculator.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 402', '11 AM', 'Wednesday', 'Trade_Completed', 0, NULL),
(79, 9, 'USB Kit Set', 1, 'Complete USB tools', '/resources/items/usb.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 501', '1 PM', 'Thursday', 'Trade_Completed', 0, NULL),
(80, 10, 'Study Lamp', 1, 'LED desk lamp', '/resources/items/mini_fan.jpg', 'Electronics', 'Good', NULL, 'Available', NULL, NULL, '2026-05-17 22:10:00', 'HPSB 502', '2 PM', 'Friday', 'Trade_Completed', 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `items_log`
--

CREATE TABLE `items_log` (
  `log_id` int(11) NOT NULL,
  `item_id` int(5) NOT NULL,
  `action` enum('Create','Update','Archive','Retrieve','Delete') NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `items_log`
--

INSERT INTO `items_log` (`log_id`, `item_id`, `action`, `reason`, `timestamp`) VALUES
(1, 1, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(2, 2, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(3, 3, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(4, 4, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(5, 5, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(6, 6, 'Update', 'Changed pickup time', '2026-05-19 02:02:54'),
(7, 7, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(8, 8, 'Create', 'Initial listing', '2026-05-19 02:02:54'),
(9, 9, 'Update', 'Lowered the price', '2026-05-19 02:02:54'),
(10, 10, 'Create', 'Initial listing', '2026-05-19 02:02:54');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(10) NOT NULL,
  `initiator_user_id` int(10) NOT NULL,
  `requester_user_id` int(10) NOT NULL,
  `message` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `initiator_user_id`, `requester_user_id`, `message`, `created_at`) VALUES
(1, 10, 1, 'Alliyah requested to buy your Java Book.', '2026-05-17 22:00:10'),
(2, 4, 2, 'Cathrina paid for your Calculator.', '2026-05-17 22:00:10'),
(3, 1, 3, 'Atasha paid for your Finance Book.', '2026-05-17 22:00:10'),
(4, 2, 4, 'Dwight bought your ID Lanyard.', '2026-05-17 22:00:10'),
(5, 6, 5, 'Dhanica requested to borrow your Chef Knife.', '2026-05-17 22:00:10'),
(6, 3, 6, 'Renzjan requested to borrow your Yoga Mat.', '2026-05-17 22:00:10'),
(7, 8, 7, 'Jasmine offered a trade for your Penlight.', '2026-05-17 22:00:10'),
(8, 9, 8, 'Julia paid for your Paint Set.', '2026-05-17 22:00:10'),
(9, 7, 9, 'Althea bought your Law Reviewer.', '2026-05-17 22:00:10'),
(10, 5, 10, 'Lindsay offered a trade for your IT Manual.', '2026-05-17 22:00:10');

-- --------------------------------------------------------

--
-- Table structure for table `payment_methods`
--

CREATE TABLE `payment_methods` (
  `payment_id` int(11) NOT NULL,
  `user_id` int(10) NOT NULL,
  `payment_type` enum('gcash','maya','BPI','BDO') NOT NULL,
  `payment_number` varchar(255) NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment_methods`
--

INSERT INTO `payment_methods` (`payment_id`, `user_id`, `payment_type`, `payment_number`, `is_active`, `created_at`) VALUES
(1, 1, 'gcash', '09171112222', 1, '2026-05-17 22:01:20'),
(2, 2, 'maya', '09183334444', 1, '2026-05-17 22:01:20'),
(3, 3, 'gcash', '09195556666', 1, '2026-05-17 22:01:20'),
(4, 4, 'BDO', '5412000011112222', 1, '2026-05-17 22:01:20'),
(5, 5, 'BPI', '4111222233334444', 1, '2026-05-17 22:01:20'),
(6, 6, 'gcash', '09221112222', 1, '2026-05-17 22:01:20'),
(7, 7, 'maya', '09233334444', 1, '2026-05-17 22:01:20'),
(8, 8, 'gcash', '09245556666', 1, '2026-05-17 22:01:20'),
(9, 9, 'BPI', '5412555566667777', 1, '2026-05-17 22:01:20'),
(10, 10, 'gcash', '09269990000', 1, '2026-05-17 22:01:20');

-- --------------------------------------------------------

--
-- Table structure for table `reputation_log`
--

CREATE TABLE `reputation_log` (
  `reputation_id` int(11) NOT NULL,
  `user_id` int(10) NOT NULL,
  `transaction_id` int(11) DEFAULT NULL,
  `points_earned` int(11) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reputation_log`
--

INSERT INTO `reputation_log` (`reputation_id`, `user_id`, `transaction_id`, `points_earned`, `timestamp`) VALUES
(1, 1, 1, 5, '2026-05-17 22:07:03'),
(2, 2, 2, 10, '2026-05-17 22:07:03'),
(3, 3, 3, 5, '2026-05-17 22:07:03'),
(4, 4, 4, 2, '2026-05-17 22:07:03'),
(5, 5, 5, 15, '2026-05-17 22:07:03'),
(6, 6, 6, 10, '2026-05-17 22:07:03'),
(7, 7, 7, 8, '2026-05-17 22:07:03'),
(8, 8, 8, 5, '2026-05-17 22:07:03'),
(9, 9, 9, 10, '2026-05-17 22:07:03'),
(10, 10, 10, 6, '2026-05-17 22:07:03');

-- --------------------------------------------------------

--
-- Table structure for table `security`
--

CREATE TABLE `security` (
  `security_id` int(10) NOT NULL,
  `user_id` int(10) NOT NULL,
  `security_question_1` varchar(255) NOT NULL,
  `security_answer_1` varchar(100) NOT NULL,
  `security_question_2` varchar(255) NOT NULL,
  `security_answer_2` varchar(100) NOT NULL,
  `security_question_3` varchar(255) NOT NULL,
  `security_answer_3` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `security`
--

INSERT INTO `security` (`security_id`, `user_id`, `security_question_1`, `security_answer_1`, `security_question_2`, `security_answer_2`, `security_question_3`, `security_answer_3`) VALUES
(1, 1, 'What was the nickname your family called you as a child?', 'Mey-mey', 'What was your favorite childhood food?', 'Sinigang', 'What is your favorite childhood movie?', 'Spirited Away'),
(2, 2, 'What was your favorite childhood food?\r\n', 'Pizza', 'What was the nickname your family called you as a child?\r\n', 'Abraham', 'What is your favorite childhood movie?', 'Cars'),
(3, 3, 'What is your favorite childhood movie?', 'Turbo', 'What was your favorite childhood food?\r\n', 'Adobo', 'What was the nickname your family called you as a child?\r\n', 'Jan-jan\r\n'),
(4, 4, 'What was the name of your first pet?\r\n', 'Tan-tan\r\n', 'What was the nickname your family called you as a child?\r\n', 'Joen', 'What was your favorite childhood food?\r\n', 'Sinigang'),
(5, 5, 'What was the nickname your family called you as a child?\r\n', 'Linds', 'What was your favorite childhood food?\r\n', 'Kare-kare', 'What is your favorite childhood movie?\r\n', 'Tangled'),
(6, 6, 'What was your favorite childhood food?\r\n', 'Sinigang', 'What was the nickname your family called you as a child?\r\n', 'Nica\r\n', 'What was the name of your first pet?\r\n', 'Oreo'),
(7, 7, 'What was the nickname your family called you as a child?\r\n', 'Tey', 'What was the name of your first pet?\r\n', 'Coffee', 'What is your favorite childhood movie?\r\n', 'Tinker bell'),
(8, 8, 'What was the nickname your family called you as a child?\r\n', 'Jas', 'What was your favorite childhood food?\r\n', 'Nilaga', 'What was the name of your first pet?\r\n', 'Santy'),
(9, 9, 'What is your favorite childhood movie?\r\n', 'Beauty and the Beast', 'What was the nickname your family called you as a child?\r\n', 'Juls', 'What was your favorite childhood food?\r\n', 'Tocino'),
(10, 10, 'What was the nickname your family called you as a child?\r\n', 'Alli', 'What is your favorite childhood movie?\r\n', 'Alice in wonderland', 'What was your favorite childhood food?\r\n', 'Pakbet');

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` int(11) NOT NULL,
  `item_id` int(5) NOT NULL,
  `borrower_id` int(10) NOT NULL,
  `quantity` int(3) DEFAULT 1,
  `total_price` int(11) DEFAULT NULL,
  `karma_impact` int(11) DEFAULT 0,
  `payment_method` enum('Cash','Gcash','Maya','BPI','BDO') DEFAULT NULL,
  `payment_reference` varchar(100) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `action` enum('Sell','Sell-Relisted','Sell-Withdrawn','Sell-Completed','Buy','Lend','Lend-Relisted','Lend-Withdrawn','Borrow-Request','Borrow-Approved','Borrow-Declined','Borrow-Return','Borrow-Completed','Trade-Relisted','Trade-Withdrawn','Trade-Initiate','Trade-Request','Trade-Approved','Trade-Declined','Trade-Completed') NOT NULL,
  `proposed_item` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transactions`
--

INSERT INTO `transactions` (`transaction_id`, `item_id`, `borrower_id`, `quantity`, `total_price`, `karma_impact`, `payment_method`, `payment_reference`, `timestamp`, `action`, `proposed_item`) VALUES
(1, 1, 10, 1, 250, 5, 'Gcash', '1643561537045', '2026-05-17 22:05:35', 'Buy', NULL),
(2, 2, 4, 1, 800, 10, 'Maya', '8523189486491', '2026-05-17 22:05:35', 'Buy', NULL),
(3, 3, 1, 1, 150, 5, 'Cash', '9685263097924', '2026-05-17 22:05:35', 'Buy', NULL),
(4, 4, 2, 1, 50, 2, 'Gcash', '3856747306749', '2026-05-17 22:05:35', 'Buy', NULL),
(5, 5, 6, 1, NULL, 15, 'Cash', NULL, '2026-05-17 22:05:35', 'Borrow-Approved', NULL),
(6, 6, 3, 1, NULL, 10, NULL, NULL, '2026-05-17 22:05:35', 'Borrow-Request', NULL),
(7, 7, 8, 1, NULL, 8, NULL, NULL, '2026-05-17 22:05:35', 'Trade-Initiate', NULL),
(8, 8, 9, 1, 300, 5, 'BPI', '7227947516579', '2026-05-17 22:05:35', 'Buy', NULL),
(9, 9, 7, 1, 500, 10, 'Gcash', '5569495939248', '2026-05-17 22:05:35', 'Buy', NULL),
(10, 10, 5, 1, NULL, 6, NULL, NULL, '2026-05-17 22:05:35', 'Trade-Request', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(10) NOT NULL,
  `student_id` varchar(9) NOT NULL,
  `system_role` enum('end_user','admin','super_admin') DEFAULT 'end_user',
  `umak_email_address` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `college` enum('CBFS','CCIS','CCSE','CET','CGPP','CITE','CTHM','CHK','ION','IOP','IIHS','IOPSY','IOA','IAD','ISW','IDEM','SOL','CCAPS','CITE-HSU') NOT NULL,
  `year_level` enum('1st Year','2nd Year','3rd Year','4th Year','5th Year','Graduate','Alumni') NOT NULL,
  `course_program` varchar(80) NOT NULL,
  `first_name` varchar(80) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `karma_score` int(11) DEFAULT 0,
  `profile_image` varchar(255) NOT NULL DEFAULT '/resources/defaultpictures/axolotl.jpg',
  `contact_num` varchar(15) DEFAULT NULL,
  `home_address` varchar(255) DEFAULT NULL,
  `users_is_archived` tinyint(1) DEFAULT 0,
  `users_archived_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `student_id`, `system_role`, `umak_email_address`, `password`, `college`, `year_level`, `course_program`, `first_name`, `last_name`, `karma_score`, `profile_image`, `contact_num`, `home_address`, `users_is_archived`, `users_archived_at`) VALUES
(1, 'K12358561', 'super_admin', 'maatasha.acebo@umak.edu.ph', 'Atasha_123', 'CCIS', '1st Year', 'BS In Information Technology', 'Ma.Atasha', 'Acebo', 150, '/resources/profilepictures/atasha_ace.jpg', '09171234567', '96 Blk 3 Hidalgo St. West Rembo, Taguig City', 0, NULL),
(2, 'K12358562', 'end_user', 'dwight.dapito@umak.edu.ph', 'dW1ght@99', 'CCIS', '1st Year', 'BS In Information Technology', 'Dwight Abraham', 'Dapito', 95, '/resources/profilepictures/dwight_d.jpg', '09187654321', '101 Tanguile St. Brgy. Cembo, Taguig City', 0, NULL),
(3, 'K12358563', 'end_user', 'renzjan.moncinilla@umak.edu.ph', 'renz123', 'CCIS', '1st Year', 'BS In Information Technology', 'Renzjan', 'Moncinilla', 110, '/resources/profilepictures/renz_monce.jpg', '09192223333', '90 Blk 5 Mangahan St. Brgy. West Rembo, Taguig City', 0, NULL),
(4, 'K12358564', 'admin', 'cathrina.lumbang@umak.edu.ph', 'catHr1n4_07', 'CCIS', '1st Year', 'BS In Information Technology', 'Cathrina', 'Lumbang', 135, '/resources/profilepictures/cath_lumbang.jpg', '09204445555', '98 Ilang-ilang St. Brgy. Pitogo, Makati City', 0, NULL),
(5, 'K12358565', 'end_user', 'lindsay.balabis@umak.edu.ph', 'L1n4say00@', 'CCIS', '1st Year', 'BS In Information Technology', 'Lindsay', 'Balabis', 50, '/resources/profilepictures/linz_b.jpg', '09216667777', '126 Sampaguita St. Brgy. Comembo, Taguig City', 0, NULL),
(6, 'K12358566', 'end_user', 'dhanica.ballesteros@umak.edu.ph', 'DH4anic4@50', 'CCIS', '1st Year', 'BS In Information Technology', 'Dhanica', 'Ballesteros', 85, '/resources/profilepictures/dhani_baller.jpg', '09228889999', '100-e San Miguel St. Brgy. Pembo, Taguig City', 0, NULL),
(7, 'K12358567', 'end_user', 'althea.aguinaldo@umak.edu.ph', 'AThe4@20', 'CCIS', '1st Year', 'BS In Information Technology', 'Althea', 'Aguinaldo', 200, '/resources/profilepictures/thea_aguinaldo.jpg', '09230001111', '267 Blk 4 Gumamela St. Brgy. East Rembo, Taguig City', 0, NULL),
(8, 'K12358568', 'end_user', 'jasmine.martizano@umak.edu.ph', 'jasMINE_11!', 'CCIS', '1st Year', 'BS In Information Technology', 'Jasmine Claire', 'Martizano', 70, '/resources/profilepictures/jas_martizano.jpg', '09243334444', '189 Bonifacio St. Brgy. Rizal, Makati City', 0, NULL),
(9, 'K12358569', 'end_user', 'julia.calamlam@umak.edu.ph', 'JUliaMarie$#11', 'CCIS', '1st Year', 'BS In Information Technology', 'Julia', 'Calamlam', 120, '/resources/profilepictures/jules_c.jpg', '09255556666', '857 Venus St. Brgy. Olympia, Makati City', 0, NULL),
(10, 'K12358570', 'end_user', 'alliyah.dumatol@umak.edu.ph', 'aLL1yah$032!', 'CCIS', '1st Year', 'BS In Information Technology', 'Alliyah', 'Dumat-ol', 90, '/resources/profilepictures/ally_dumatol.jpg', '09267778888', '99 T. Alonzo St. Brgy. Bangkal, Makati City', 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users_log`
--

CREATE TABLE `users_log` (
  `log_id` int(11) NOT NULL,
  `user_id` int(10) NOT NULL,
  `action` enum('Create','Update','Archive','Retrieve','Delete') NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users_log`
--

INSERT INTO `users_log` (`log_id`, `user_id`, `action`, `timestamp`) VALUES
(1, 1, 'Create', '2026-05-17 22:05:02'),
(2, 2, 'Create', '2026-05-17 22:05:02'),
(3, 3, 'Create', '2026-05-17 22:05:02'),
(4, 4, 'Create', '2026-05-17 22:05:02'),
(5, 5, 'Create', '2026-05-17 22:05:02'),
(6, 6, 'Update', '2026-05-17 22:05:02'),
(7, 7, 'Update', '2026-05-17 22:05:02'),
(8, 8, 'Create', '2026-05-17 22:05:02'),
(9, 9, 'Update', '2026-05-17 22:05:02'),
(10, 10, 'Create', '2026-05-17 22:05:02');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `owner_id` (`owner_id`);

--
-- Indexes for table `items_log`
--
ALTER TABLE `items_log`
  ADD PRIMARY KEY (`log_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `fk_notifications_initiator` (`initiator_user_id`),
  ADD KEY `fk_notifications_requester` (`requester_user_id`);

--
-- Indexes for table `payment_methods`
--
ALTER TABLE `payment_methods`
  ADD PRIMARY KEY (`payment_id`),
  ADD UNIQUE KEY `unique_user_payment` (`user_id`,`payment_type`);

--
-- Indexes for table `reputation_log`
--
ALTER TABLE `reputation_log`
  ADD PRIMARY KEY (`reputation_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `transaction_id` (`transaction_id`);

--
-- Indexes for table `security`
--
ALTER TABLE `security`
  ADD PRIMARY KEY (`security_id`),
  ADD KEY `fk_security_user` (`user_id`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `item_id` (`item_id`),
  ADD KEY `borrower_id` (`borrower_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `student_id` (`student_id`),
  ADD UNIQUE KEY `umak_email_address` (`umak_email_address`);

--
-- Indexes for table `users_log`
--
ALTER TABLE `users_log`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `item_id` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=81;

--
-- AUTO_INCREMENT for table `items_log`
--
ALTER TABLE `items_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `payment_methods`
--
ALTER TABLE `payment_methods`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `reputation_log`
--
ALTER TABLE `reputation_log`
  MODIFY `reputation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `security`
--
ALTER TABLE `security`
  MODIFY `security_id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `users_log`
--
ALTER TABLE `users_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `items`
--
ALTER TABLE `items`
  ADD CONSTRAINT `items_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notifications_initiator` FOREIGN KEY (`initiator_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_notifications_requester` FOREIGN KEY (`requester_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `payment_methods`
--
ALTER TABLE `payment_methods`
  ADD CONSTRAINT `payment_methods_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `reputation_log`
--
ALTER TABLE `reputation_log`
  ADD CONSTRAINT `reputation_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reputation_log_ibfk_2` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`) ON DELETE SET NULL;

--
-- Constraints for table `security`
--
ALTER TABLE `security`
  ADD CONSTRAINT `fk_security_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`borrower_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `users_log`
--
ALTER TABLE `users_log`
  ADD CONSTRAINT `users_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
