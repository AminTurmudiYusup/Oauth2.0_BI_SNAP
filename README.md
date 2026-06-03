# BI SNAP OAuth 2.0 Project

This project demonstrates the implementation of **OAuth 2.0** based on the **BI SNAP (Bank Indonesia SNAP)** standard, focusing on secure communication in B2B and B2B2C scenarios.

## 📌 Overview

The goal of this project is to provide a practical example of how OAuth 2.0 can be implemented for secure API authentication and authorization in financial service integrations, especially following BI SNAP guidelines.

## ⚙️ Prerequisites

Before running this project, make sure you have:

1. Basic knowledge of importing projects into IDEs such as IntelliJ IDEA or Eclipse
2. PostgreSQL installed and running on your machine
3. Understanding of OAuth 2.0 concepts
4. Understanding of B2B (Business-to-Business) integration
5. Basic knowledge of cryptography:

   * Asymmetric signatures
   * Symmetric signatures
   * JWT access token

## 🏗️ Project Architecture

This project is designed with a modular architecture to simulate secure communication between services using OAuth 2.0 and digital signatures.

<img width="558" height="433" alt="new_security" src="https://github.com/user-attachments/assets/3b36c934-8e93-47cf-ae0d-0adf2121465a" />


## 🔄 Flow B2B

This flow demonstrates how two backend systems (Company-to-Company) communicate securely using OAuth 2.0 and signed requests.

<img width="1310" height="1054" alt="b2b activity diagram" src="https://github.com/user-attachments/assets/28b2f232-65f8-4672-bc91-268d6966a8da" />



## 🔄 Flow B2B2C

This flow extends the B2B scenario to include end users (Customer), showing how authentication and authorization are handled across multiple parties.


<img width="1770" height="1747" alt="b2b2c_activity_diagram" src="https://github.com/user-attachments/assets/ef9fd162-8662-4725-9890-690477c19bc9" />


## 🚀 Getting Started

1. Clone the repository
2. Configure your PostgreSQL database(run sql in postgressql)
3. Import the project into your preferred IDE
4. Run the application

## 📎 Notes

This project is develop base on BI SNAP standards.

