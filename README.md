# Card API - Hyperativa Challenge

A secure REST API for registering and querying complete credit card numbers with JWT authentication.

## 📋 About the Project

This is a robust API developed in **Java with Spring Boot 3.5.10** that implements:

- ✅ JWT Authentication with HS512
- ✅ AES-128 Encryption for card data
- ✅ Individual and batch registration via TXT file
- ✅ Secure card queries with user authorization
- ✅ Structured logging with SLF4J
- ✅ Unit tests (Mockito) and integration tests (MockMvc + H2)
- ✅ H2 in-memory database (development) / MySQL (production)
- ✅ Robust input validation with Bean Validation
- ✅ Global exception handling with @ControllerAdvice
- ✅ Interactive API documentation with Swagger UI (OpenAPI 3.0)

## 🚀 Quick Start

### Prerequisites

#### Option 1: Local Execution
- **Java 25+**
- **Maven 3.8+**
- **Git**

#### Option 2: Docker Compose
- **Docker** (version 20.10+)
- **Docker Compose** (version 1.29+)
- **Git**

### Installation and Execution

#### Option 1: Local Execution with Maven

```bash
# Clone the repository
git clone <your-repository-url>
cd back-end-hyperativa

# Compile the project
mvn clean install

# Run the tests
mvn test

# Start the application
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

#### Option 2: Execution with Docker Compose (Recommended)

```bash
# Clone the repository
git clone <your-repository-url>
cd back-end-hyperativa

# Start the containers (MySQL + API)
docker-compose up -d --build

# Monitor the logs
docker-compose logs -f hyperativa-api

# Wait for the message: "Started BackEndHyperativaApplication"
```

**Useful Docker Compose commands:**

```bash
# Check container status
docker-compose ps

# View API logs
docker-compose logs -f hyperativa-api

# View database logs
docker-compose logs -f db

# Stop containers
docker-compose down

# Stop and remove volumes (clean data)
docker-compose down -v

# Rebuild the image (after code changes)
docker-compose up -d --build

# Restart containers
docker-compose restart
```

**Database credentials** (pre-configured in `docker-compose.yaml`):
- MySQL Root Password: `root`
- MySQL Database: `hyperativa_db`
- MySQL User: `hyperativa`
- MySQL Password: `hyperativa123`
- API Port: `8080`

The API will be available at: `http://localhost:8080`
The application will automatically connect to MySQL at `db:3306`

---

### Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **H2 Console** (local only): http://localhost:8080/h2-console

## 🐳 Docker Compose Troubleshooting

### Issue: API container doesn't start

```bash
# Check detailed logs
docker-compose logs hyperativa-api

# Check if MySQL is healthy
docker-compose logs db

# Force containers to be recreated
docker-compose down
docker-compose up -d --build
```

### Issue: Port 3306 (MySQL) is already in use

```bash
# Option 1: Stop local MySQL
sudo systemctl stop mysql

# Option 2: Change the port in docker-compose.yaml:
# Change "3306:3306" to "3307:3306" in the "db: ports" section
# Then:
docker-compose down
docker-compose up -d
```

### Issue: Port 8080 (API) is already in use

```bash
# Option 1: Stop the local application
sudo kill $(lsof -t -i:8080)

# Option 2: Change the port in docker-compose.yaml:
# Change "8080:8080" to "8081:8080" in the "hyperativa-api: ports" section
docker-compose down
docker-compose up -d
```

### Issue: Database connection error

```bash
# Check if MySQL container is healthy
docker-compose ps

# If status is not "Up", rebuild:
docker-compose down -v
docker-compose up -d

# Wait 30 seconds for MySQL to fully initialize
sleep 30

# Check logs
docker-compose logs db
```

### Clean all data (complete reset)

```bash
# Remove containers and volumes (WARNING: deletes all data)
docker-compose down -v

# Restart from scratch
docker-compose up -d

# Monitor initialization
docker-compose logs -f
```

### Issue: Error "container already exists"

```bash
# Remove old containers
docker-compose rm -f

# Then start again
docker-compose up -d
```

---

## 🔐 JWT Authentication

All endpoints, except `/v1/auth/register` and `/v1/auth/token`, require JWT authentication.

### JWT Token

- **Default duration**: 24 hours (86,400,000 ms)
- **Algorithm**: HS512
- **Authentication type**: Bearer Token
- **Expected header**: `Authorization: Bearer <token>`

## 📚 API Endpoints

### 1. Authentication

#### Register New User
```http
POST /v1/auth/register
Content-Type: application/json

{
  "username": "your_username",
  "email": "your_email@example.com",
  "password": "your_secure_password"
}
```

**Response (201 Created):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "your_username"
}
```

**Validations:**
- Username: 3 to 50 characters
- Email: valid format and unique
- Password: minimum 6 characters

---

#### Login
```http
POST /v1/auth/token
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_secure_password"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "your_username"
}
```

**Possible Errors:**
- `401 Unauthorized`: Invalid credentials

---

### 2. Card Management

#### Create Individual Card
```http
POST /v1/cards
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "cardNumber": "4456897999999999",
  "cardIdentifier": "CARD_001"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "cardIdentifier": "CARD_001",
  "cardNumberLastDigits": "9999",
  "createdAt": "2026-02-16T10:30:00"
}
```

**Validations:**
- cardNumber: 13 to 19 digits (required)
- cardIdentifier: cannot be duplicated (required)

**Possible Errors:**
- `400 Bad Request`: Validation failed
- `403 Forbidden`: Not authenticated
- `409 Conflict`: Card already exists

---

#### Batch File Upload
```http
POST /v1/cards/batch
Authorization: Bearer <your_jwt_token>
Content-Type: multipart/form-data

file: <txt_file>
```

**Expected TXT file format:**
```
DESAFIO-HYPERATIVA           20260216LOTE0001    002
C1      4456897000000001
C2      4456897000000002
LOTE        LOTE0001      002
```

**Response (200 OK):**
```json
{
  "loteNumber": "LOTE0001",
  "totalCards": 2,
  "processedCards": 2,
  "status": "COMPLETED"
}
```

**Possible Responses:**
- `200 COMPLETED`: All cards were processed
- `200 FAILED`: No valid cards found
- `400 Bad Request`: Empty file
- `403 Forbidden`: Not authenticated

---

#### Get Card by ID
```http
GET /v1/cards/{cardId}
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "cardIdentifier": "CARD_001",
  "cardNumberLastDigits": "9999",
  "createdAt": "2026-02-16T10:30:00"
}
```

**Possible Errors:**
- `403 Forbidden`: Card belongs to another user
- `404 Not Found`: Card not found

---

#### Search Card by Number
```http
GET /v1/cards/search
Authorization: Bearer <your_jwt_token>
Header: cardNumber: 4456897999999999
```

**Response (200 OK):**
```json
{
  "id": 1,
  "cardIdentifier": "CARD_001",
  "cardNumberLastDigits": "9999",
  "createdAt": "2026-02-16T10:30:00"
}
```

**Possible Errors:**
- `403 Forbidden`: Not authenticated
- `404 Not Found`: Card not found

## 🔒 Security

### Encryption

- **Card numbers**: Encrypted with AES-128 before storage
- **Passwords**: Hash with BCrypt
- **JWT Tokens**: Signed with HS512

### Environment Variables (Recommended in Production)

```bash
# JWT
JWT_SECRET=your-very-secure-secret-key
JWT_EXPIRATION_MS=86400000

# Encryption
CRYPTO_SECRET=your-very-secure-encryption-key

# Server
SERVER_PORT=8080
```

## 🗄️ Database

### Schema

**Table: users**
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Table: cards**
```sql
CREATE TABLE cards (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  card_number_encrypted TEXT NOT NULL,
  card_number_last_digits VARCHAR(4) NOT NULL,
  card_identifier VARCHAR(50) NOT NULL UNIQUE,
  lote_number VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_user_id (user_id),
  INDEX idx_card_number_last_digits (card_number_last_digits),
  INDEX idx_card_identifier (card_identifier)
);
```

**Table: card_batches**
```sql
CREATE TABLE card_batches (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lote_number VARCHAR(50) UNIQUE NOT NULL,
  total_cards INT NOT NULL,
  processed_cards INT DEFAULT 0,
  status VARCHAR(20) DEFAULT 'PENDING',
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_lote_number (lote_number),
  INDEX idx_batch_user_id (user_id)
);
```

## 📊 Logging

Logs are saved in `logs/cardapi.log` with the following levels:

- **INFO**: Main operations (login, card creation, uploads)
- **DEBUG**: Read operations, searches
- **WARN**: Duplication attempts, unauthorized access
- **ERROR**: Exceptions and system errors

**Example log:**
```
2024-02-10 10:30:00 - AuthController - Register endpoint called for userEntity: your_username
2024-02-10 10:30:05 - CardController - Create cardEntity endpoint called by userEntity: your_username - IP: 192.168.1.100
2024-02-10 10:30:10 - CardService - Card created successfully with id: 1 for userEntity: your_username
```

## 🧪 Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Tests
```bash
# Encryption tests
mvn test -Dtest=CryptoServiceTest

# JWT tests
mvn test -Dtest=JwtTokenProviderTest

# File parser tests
mvn test -Dtest=FileParserServiceTest

# Authentication tests
mvn test -Dtest=AuthControllerTest

# Card tests
mvn test -Dtest=CardControllerTest
```

### Test Coverage

The test suite covers:
- ✅ Encryption and decryption
- ✅ JWT generation and validation
- ✅ TXT file parsing
- ✅ User registration and login
- ✅ Card CRUD operations
- ✅ Authorization and authentication
- ✅ Input validations
- ✅ Error handling

## 🏗️ Architecture

### Folder Structure

```
src/main/java/com/hyperativa/back_end/
├── config/              # Configurations (SecurityConfig)
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
├── exception/           # Custom Exceptions
├── repository/          # Data Access
├── security/            # JWT and Encryption
├── service/             # Business Logic
└── util/                # Utilities (Parser)
```

### Design Patterns Used

- **MVC**: Model-View-Controller
- **DTO**: Data Transfer Object
- **JPA/Hibernate**: ORM
- **BCrypt**: Password hashing
- **JWT**: Stateless authentication
- **AES-128**: Card encryption

## 📈 Scalability

The API was designed for high scalability:

- **Índices de banco de dados**: Otimizados para buscas por usuario_id e card_identifier
- **Criptografia em camada**: Apenas dados sensíveis são criptografados
- **Lazy Loading**: Relacionamentos JPA com fetch type LAZY
- **Connection Pool**: HikariCP (padrão do Spring Boot)

## 🔧 Production Configuration

### 1. Change Database

Edit `application.yml` to use PostgreSQL or MySQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cardapi
    driver-class-name: org.postgresql.Driver
    username: user
    password: pass
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect
```

### 2. Environment Variables

```bash
export JWT_SECRET="your-super-secure-key-here"
export CRYPTO_SECRET="your-super-secure-encryption-key-here"
export SERVER_PORT=8080
```

### 3. Build for Production

```bash
mvn clean package -DskipTests
java -jar target/back-end-hyperativa-0.0.1-SNAPSHOT.jar
```

### 4. Docker (Optional)

```dockerfile
FROM openjdk:25-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t card-api .
docker run -p 8080:8080 card-api
```

## 📝 Exemplo de Uso Completo

### 1. Registrar Usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao",
    "email": "joao@example.com",
    "password": "senha123"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao",
    "password": "senha@123"
  }'
```

Save the `accessToken` received.

### 3. Create Card

```bash
curl -X POST http://localhost:8080/v1/cards \
  -H "Authorization: Bearer <your-token-here>" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4456897999999999",
    "cardIdentifier": "CARD_001"
  }'
```

### 4. Upload Batch File

```bash
curl -X POST http://localhost:8080/v1/cards/batch \
  -H "Authorization: Bearer <your-token-here>" \
  -F "file=@cards.txt"
```

### 5. Search Card

```bash
curl -X GET "http://localhost:8080/v1/cards/search" \
  -H "Authorization: Bearer <your-token-here>" \
  -H "cardNumber: 4456897999999999"
```

## 🤝 Contributing

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

Developed as a solution for the Hyperativa Challenge.

## ❓ Support

For questions or issues, open an issue in the repository.

---

**Last update**: February 2026
