# 🚀 Guia para Criar o Backend do Sabixão

## Opção 1: Spring Boot REST API (Recomendado)

### 1. Criar projeto Spring Boot

Use o Spring Initializr (https://start.spring.io/) com:
- **Project:** Maven
- **Language:** Java
- **Spring Boot:** 3.2.x
- **Java:** 17 ou 11
- **Dependencies:**
  - Spring Web
  - Spring Data JPA
  - H2 Database (para dev) ou PostgreSQL/MySQL
  - Spring Security
  - Lombok (opcional, mas recomendado)
  - Validation

### 2. Estrutura do Projeto Backend

```
backend/
├── src/main/java/com/grupo2/sabixao/
│   ├── SabixaoApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── UserController.java
│   │   └── QuizController.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Quiz.java
│   │   ├── Question.java
│   │   └── Answer.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── QuizRepository.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── QuizService.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── UserResponse.java
└── src/main/resources/
    └── application.properties
```

### 3. Exemplo de Código

#### UserController.java
```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.getNome(), request.getSenha());
            return ResponseEntity.ok(new UserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getNome(), request.getSenha());
            return ResponseEntity.ok(new UserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }
}
```

#### User.java (Entity)
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private String senha; // Será armazenada com hash
    
    @Column
    private Integer pontuacao = 0;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
```

#### UserService.java
```java
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String nome, String senha) {
        if (userRepository.existsByNome(nome)) {
            throw new RuntimeException("Usuário já existe!");
        }
        
        User user = new User();
        user.setNome(nome);
        user.setSenha(passwordEncoder.encode(senha));
        user.setPontuacao(0);
        
        return userRepository.save(user);
    }

    public User authenticate(String nome, String senha) {
        User user = userRepository.findByNome(nome)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        if (!passwordEncoder.matches(senha, user.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }
        
        return user;
    }
}
```

#### QuizController.java
```java
@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @GetMapping("/validate-pin")
    public ResponseEntity<?> validatePin(@RequestParam String pin) {
        boolean isValid = quizService.validatePin(pin);
        if (isValid) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("valid", false, "message", "PIN inválido"));
    }

    @GetMapping("/{pin}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable String pin) {
        List<Question> questions = quizService.getQuestionsByPin(pin);
        return ResponseEntity.ok(questions);
    }
}
```

#### application.properties
```properties
# Server
server.port=8080

# Database H2 (Development)
spring.datasource.url=jdbc:h2:mem:sabixao
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# CORS
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
```

### 4. Configuração de Segurança

#### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                .requestMatchers("/api/quiz/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers().frameOptions().disable();
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Opção 2: Backend Leve com Javalin (Mais Simples)

Para um backend ainda mais leve e rápido:

### pom.xml
```xml
<dependency>
    <groupId>io.javalin</groupId>
    <artifactId>javalin</artifactId>
    <version>5.6.3</version>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### Main.java
```java
public class Main {
    private static Map<String, User> users = new HashMap<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(8080);

        // Registrar usuário
        app.post("/api/users/register", ctx -> {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            
            if (users.containsKey(req.getNome())) {
                ctx.status(400).result("Usuário já existe");
                return;
            }
            
            User user = new User(req.getNome(), req.getSenha());
            users.put(req.getNome(), user);
            ctx.json(user);
        });

        // Login
        app.post("/api/users/login", ctx -> {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            User user = users.get(req.getNome());
            
            if (user == null || !user.getSenha().equals(req.getSenha())) {
                ctx.status(401).result("Credenciais inválidas");
                return;
            }
            
            ctx.json(user);
        });

        // Validar PIN
        app.get("/api/quiz/validate-pin", ctx -> {
            String pin = ctx.queryParam("pin");
            // Lógica de validação
            ctx.json(Map.of("valid", true));
        });
    }
}
```

## 🧪 Testando a API

### Com cURL:

```bash
# Registrar usuário
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"João","senha":"senha123"}'

# Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"nome":"João","senha":"senha123"}'

# Validar PIN
curl http://localhost:8080/api/quiz/validate-pin?pin=12345
```

### Com Postman:
1. Importe a coleção de requisições
2. Configure as variáveis de ambiente
3. Teste cada endpoint

## 📚 Recursos Úteis

- Spring Boot Docs: https://spring.io/projects/spring-boot
- Javalin Docs: https://javalin.io/
- H2 Database: https://www.h2database.com/
- REST API Design: https://restfulapi.net/

## 🎯 Próximos Passos

1. Escolher entre Spring Boot ou Javalin
2. Implementar os endpoints básicos
3. Adicionar validações e tratamento de erros
4. Criar sistema de quizzes e perguntas
5. Implementar sistema de pontuação
6. Adicionar testes unitários
