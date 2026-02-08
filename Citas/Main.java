package Citas;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * PROGRAMA: Sistema de administración de citas (consola)
 * ----------------------------------------------------
 * Este archivo contiene TODO el sistema en un solo .java para que sea sencillo de entregar y ejecutar.
 *
 * ¿Qué hace?
 * 1) Permite iniciar sesión como administrador.
 * 2) Permite dar de alta doctores y pacientes (con IDs únicos).
 * 3) Permite crear citas relacionadas a un doctor y un paciente.
 * 4) Permite guardar y cargar toda la información desde archivos CSV (texto plano).
 *
 * Persistencia:
 * - doctors.csv
 * - pacientes.csv
 * - citas.csv
 * - usuarios.csv
 *
 * Separador de campos: ';'
 */
public class Main {

    // =======================
    // MODELO
    // =======================

    /**
     * CLASE Persona
     * -------------
     * Representa a una persona genérica con datos básicos compartidos por Doctor y Paciente.
     *
     * Responsabilidad:
     * - Guardar un identificador único (id).
     * - Guardar el nombre completo.
     *
     * Nota:
     * - Es "inmutable" (final) para simplificar el programa: una vez creada, no se modifica.
     * - Se usa herencia para que Doctor y Paciente reutilicen este código.
     */
    static class Persona {
        /** Identificador único de la persona (no debe repetirse en su lista). */
        private final String id;

        /** Nombre completo de la persona. */
        private final String nombreCompleto;

        /**
         * CONSTRUCTOR Persona
         * -------------------
         * Recibe los datos básicos y los guarda.
         *
         * @param id Identificador único.
         * @param nombreCompleto Nombre completo.
         */
        public Persona(String id, String nombreCompleto) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
        }

        /**
         * getId()
         * -------
         * Devuelve el identificador único de la persona.
         *
         * @return id
         */
        public String getId() { return id; }

        /**
         * getNombreCompleto()
         * -------------------
         * Devuelve el nombre completo de la persona.
         *
         * @return nombreCompleto
         */
        public String getNombreCompleto() { return nombreCompleto; }
    }

    /**
     * CLASE Doctor (extiende Persona)
     * -------------------------------
     * Representa un doctor del consultorio.
     *
     * Responsabilidad:
     * - Ser una Persona (tener id y nombre).
     * - Agregar el dato extra: especialidad.
     *
     * Nota:
     * - Hereda de Persona para no duplicar código.
     */
    static class Doctor extends Persona {
        /** Especialidad del doctor (ej: "Cardiología"). */
        private final String especialidad;

        /**
         * CONSTRUCTOR Doctor
         * ------------------
         * Construye un Doctor recibiendo sus datos y llamando al constructor de Persona
         * para inicializar id y nombre.
         *
         * @param id Identificador único.
         * @param nombreCompleto Nombre completo.
         * @param especialidad Especialidad médica.
         */
        public Doctor(String id, String nombreCompleto, String especialidad) {
            super(id, nombreCompleto);
            this.especialidad = especialidad;
        }

        /**
         * getEspecialidad()
         * -----------------
         * Devuelve la especialidad del doctor.
         *
         * @return especialidad
         */
        public String getEspecialidad() { return especialidad; }
    }

    /**
     * CLASE Paciente (extiende Persona)
     * ---------------------------------
     * Representa a un paciente del consultorio.
     *
     * Responsabilidad:
     * - Ser una Persona (tener id y nombre).
     *
     * Nota:
     * - No agrega nuevos atributos, pero existe como clase propia porque:
     *   1) El sistema lo maneja en una lista distinta.
     *   2) Semánticamente, un paciente es diferente a un doctor.
     */
    static class Paciente extends Persona {

        /**
         * CONSTRUCTOR Paciente
         * --------------------
         * Construye un Paciente usando id y nombre. Reutiliza el constructor de Persona.
         *
         * @param id Identificador único del paciente.
         * @param nombreCompleto Nombre completo del paciente.
         */
        public Paciente(String id, String nombreCompleto) {
            super(id, nombreCompleto);
        }
    }

    /**
     * CLASE Cita
     * ----------
     * Representa una cita médica.
     *
     * Responsabilidad:
     * - Guardar los datos básicos de la cita:
     *   - id único de la cita
     *   - fecha y hora (como texto, recomendado ISO-8601)
     *   - motivo
     *   - doctorId (relación con Doctor)
     *   - pacienteId (relación con Paciente)
     *
     * Nota:
     * - Guardamos doctorId y pacienteId (en vez de objetos) para simplificar el guardado en CSV.
     * - En un sistema más avanzado, podrías manejar fechas con LocalDateTime.
     */
    static class Cita {
        private final String id;
        private final String fechaHora;   // ISO-8601 recomendado
        private final String motivo;
        private final String doctorId;
        private final String pacienteId;

        /**
         * CONSTRUCTOR Cita
         * ----------------
         * Inicializa todos los campos de la cita.
         *
         * @param id Identificador único.
         * @param fechaHora Fecha y hora en texto (ej: 2026-02-08T10:30).
         * @param motivo Motivo de la cita.
         * @param doctorId ID del doctor asociado.
         * @param pacienteId ID del paciente asociado.
         */
        public Cita(String id, String fechaHora, String motivo, String doctorId, String pacienteId) {
            this.id = id;
            this.fechaHora = fechaHora;
            this.motivo = motivo;
            this.doctorId = doctorId;
            this.pacienteId = pacienteId;
        }

        /**
         * getId()
         * -------
         * Devuelve el ID de la cita.
         */
        public String getId() { return id; }

        /**
         * getFechaHora()
         * -------------
         * Devuelve la fecha y hora (en texto).
         */
        public String getFechaHora() { return fechaHora; }

        /**
         * getMotivo()
         * -----------
         * Devuelve el motivo de la cita.
         */
        public String getMotivo() { return motivo; }

        /**
         * getDoctorId()
         * -------------
         * Devuelve el id del doctor asociado.
         */
        public String getDoctorId() { return doctorId; }

        /**
         * getPacienteId()
         * ---------------
         * Devuelve el id del paciente asociado.
         */
        public String getPacienteId() { return pacienteId; }
    }

    /**
     * CLASE Usuario
     * -------------
     * Representa un usuario del sistema para control de acceso.
     *
     * Responsabilidad:
     * - Guardar credenciales:
     *   - username
     *   - passwordHash (nunca guardar la contraseña en texto plano)
     * - Guardar el rol (por ejemplo ADMIN).
     * - Verificar contraseña comparando hash.
     *
     * Nota:
     * - Se usa SHA-256 para mantenerlo simple.
     * - En vida real se usa SALT + algoritmos como bcrypt/argon2, etc.
     */
    static class Usuario {
        private final String id;
        private final String username;
        private final String passwordHash;
        private final String rol; // "ADMIN"

        /**
         * CONSTRUCTOR Usuario
         * -------------------
         * Guarda datos del usuario. Aquí ya se espera el passwordHash calculado.
         *
         * @param id Identificador único del usuario.
         * @param username Nombre de usuario para login.
         * @param passwordHash Hash de la contraseña.
         * @param rol Rol del usuario (ej: ADMIN).
         */
        public Usuario(String id, String username, String passwordHash, String rol) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
            this.rol = rol;
        }

        /** Devuelve el id del usuario. */
        public String getId() { return id; }

        /** Devuelve el username. */
        public String getUsername() { return username; }

        /** Devuelve el hash almacenado. */
        public String getPasswordHash() { return passwordHash; }

        /** Devuelve el rol (ADMIN, etc). */
        public String getRol() { return rol; }

        /**
         * verificarPassword()
         * -------------------
         * Recibe una contraseña en texto plano, la convierte a SHA-256 y la compara
         * con el hash almacenado.
         *
         * @param passwordPlano contraseña escrita por el usuario en consola.
         * @return true si el hash coincide, false si no coincide.
         */
        public boolean verificarPassword(String passwordPlano) {
            return sha256(passwordPlano).equals(this.passwordHash);
        }

        /**
         * esAdmin()
         * ---------
         * Indica si el rol del usuario es ADMIN.
         *
         * @return true si es ADMIN, false en otro caso.
         */
        public boolean esAdmin() {
            return "ADMIN".equalsIgnoreCase(this.rol);
        }
    }

    // =======================
    // USER MANAGER
    // =======================

    /**
     * CLASE UserManager
     * -----------------
     * Encapsula el control de acceso:
     * - Registro de administradores
     * - Login / logout
     * - Saber quién es el usuario actual
     * - Validar si el usuario actual es admin
     *
     * ¿Por qué existe?
     * - Para separar la lógica de usuarios del resto del sistema (doctores/pacientes/citas).
     */
    static class UserManager {
        /** Lista de usuarios registrados en el sistema. */
        private List<Usuario> usuarios = new ArrayList<>();

        /** Usuario que inició sesión actualmente. Si es null, no hay sesión. */
        private Usuario usuarioActual = null;

        /**
         * registrarAdmin()
         * ----------------
         * Crea un usuario administrador.
         *
         * Flujo:
         * 1) Verifica que el username no exista.
         * 2) Calcula el hash SHA-256 de la contraseña.
         * 3) Agrega el usuario con rol ADMIN.
         *
         * @param id ID del usuario.
         * @param username Username único.
         * @param password Contraseña en texto plano (se hashea).
         */
        public void registrarAdmin(String id, String username, String password) {
            for (Usuario u : usuarios) {
                if (u.getUsername().equals(username)) {
                    throw new RuntimeException("Username ya existe");
                }
            }
            String hash = sha256(password);
            usuarios.add(new Usuario(id, username, hash, "ADMIN"));
        }

        /**
         * login()
         * -------
         * Intenta iniciar sesión con username y password.
         *
         * Flujo:
         * 1) Busca el usuario por username.
         * 2) Si lo encuentra, verifica el password (comparando hashes).
         * 3) Si es correcto, asigna usuarioActual y regresa true.
         *
         * @param username Nombre de usuario.
         * @param password Contraseña en texto plano.
         * @return true si inició sesión, false si falló.
         */
        public boolean login(String username, String password) {
            for (Usuario u : usuarios) {
                if (u.getUsername().equals(username)) {
                    if (u.verificarPassword(password)) {
                        usuarioActual = u;
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        /**
         * logout()
         * --------
         * Cierra la sesión actual poniendo usuarioActual en null.
         */
        public void logout() { usuarioActual = null; }

        /**
         * estaAutenticado()
         * -----------------
         * Indica si hay un usuario logueado actualmente.
         *
         * @return true si usuarioActual != null
         */
        public boolean estaAutenticado() { return usuarioActual != null; }

        /**
         * esAdmin()
         * ---------
         * Valida si el usuario actual existe y además tiene rol ADMIN.
         *
         * @return true si hay sesión y es ADMIN, false en otro caso.
         */
        public boolean esAdmin() {
            if (usuarioActual == null) return false;
            return usuarioActual.esAdmin();
        }

        /**
         * getUsuarioActual()
         * ------------------
         * Devuelve el usuario actualmente logueado (o null si no hay).
         *
         * @return Usuario actual o null.
         */
        public Usuario getUsuarioActual() { return usuarioActual; }

        /**
         * setUsuarios()
         * -------------
         * Reemplaza la lista de usuarios (por ejemplo, después de cargar del CSV).
         * Por seguridad, reinicia la sesión (usuarioActual = null).
         *
         * @param users lista de usuarios cargados.
         */
        public void setUsuarios(List<Usuario> users) {
            this.usuarios = users;
            this.usuarioActual = null;
        }

        /**
         * getUsuarios()
         * -------------
         * Devuelve la lista completa de usuarios registrados.
         *
         * @return lista de usuarios.
         */
        public List<Usuario> getUsuarios() { return usuarios; }
    }

    // =======================
    // STORAGE CSV
    // =======================

    /**
     * CLASE CsvStorage
     * ---------------
     * Se encarga de la persistencia a archivos CSV (texto plano).
     *
     * Responsabilidades:
     * - Crear la carpeta si no existe.
     * - Guardar y cargar:
     *   - doctores
     *   - pacientes
     *   - citas
     *   - usuarios
     *
     * Nota:
     * - Usamos ';' como separador para evitar problemas comunes con comas en nombres.
     * - Implementamos un "escape" muy simple para manejar ';' y saltos de línea.
     */
    static class CsvStorage {
        private final File doctorsFile;
        private final File pacientesFile;
        private final File citasFile;
        private final File usuariosFile;

        /**
         * CONSTRUCTOR CsvStorage
         * ----------------------
         * Define los archivos dentro de una carpeta (folderPath). Si la carpeta no existe,
         * se crea automáticamente.
         *
         * @param folderPath carpeta donde se guardarán los CSV.
         */
        public CsvStorage(String folderPath) {
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            this.doctorsFile = new File(folder, "doctors.csv");
            this.pacientesFile = new File(folder, "pacientes.csv");
            this.citasFile = new File(folder, "citas.csv");
            this.usuariosFile = new File(folder, "usuarios.csv");
        }

        /**
         * esc()
         * -----
         * Convierte un texto para que sea seguro guardarlo en una línea CSV.
         * Reemplaza:
         * - ';' por '\;'
         * - salto de línea por '\n'
         * - retorno de carro por '\r'
         *
         * Esto evita que el CSV se "rompa" si el usuario escribe caracteres especiales.
         *
         * @param s texto original
         * @return texto escapado
         */
        private String esc(String s) {
            if (s == null) return "";
            return s.replace(";", "\\;")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
        }

        /**
         * unesc()
         * -------
         * Hace el proceso inverso de esc(): convierte el texto escapado al texto real.
         *
         * @param s texto escapado
         * @return texto normal
         */
        private String unesc(String s) {
            if (s == null) return "";
            return s.replace("\\r", "\r")
                    .replace("\\n", "\n")
                    .replace("\\;", ";");
        }

        /**
         * cargarDoctores()
         * ---------------
         * Lee doctors.csv y construye una lista de doctores.
         *
         * - Si el archivo no existe, regresa lista vacía (sistema sin doctores aún).
         * - Si hay error de lectura, lanza RuntimeException para mostrar un mensaje claro.
         *
         * @return lista de doctores cargados desde archivo.
         */
        public List<Doctor> cargarDoctores() {
            List<Doctor> list = new ArrayList<>();
            if (!doctorsFile.exists()) return list;

            try (BufferedReader br = new BufferedReader(new FileReader(doctorsFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = splitSemi(line);
                    if (parts.length < 3) continue;

                    String id = unesc(parts[0]);
                    String nombre = unesc(parts[1]);
                    String esp = unesc(parts[2]);

                    list.add(new Doctor(id, nombre, esp));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error leyendo doctors.csv: " + e.getMessage());
            }
            return list;
        }

        /**
         * guardarDoctores()
         * -----------------
         * Sobrescribe por completo doctors.csv con la lista de doctores actual.
         *
         * @param doctores lista de doctores a guardar.
         */
        public void guardarDoctores(List<Doctor> doctores) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(doctorsFile, StandardCharsets.UTF_8))) {
                for (Doctor d : doctores) {
                    bw.write(esc(d.getId()) + ";" + esc(d.getNombreCompleto()) + ";" + esc(d.getEspecialidad()));
                    bw.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error escribiendo doctors.csv: " + e.getMessage());
            }
        }

        /**
         * cargarPacientes()
         * -----------------
         * Lee pacientes.csv y construye la lista de pacientes.
         *
         * @return lista de pacientes.
         */
        public List<Paciente> cargarPacientes() {
            List<Paciente> list = new ArrayList<>();
            if (!pacientesFile.exists()) return list;

            try (BufferedReader br = new BufferedReader(new FileReader(pacientesFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = splitSemi(line);
                    if (parts.length < 2) continue;

                    String id = unesc(parts[0]);
                    String nombre = unesc(parts[1]);

                    list.add(new Paciente(id, nombre));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error leyendo pacientes.csv: " + e.getMessage());
            }
            return list;
        }

        /**
         * guardarPacientes()
         * ------------------
         * Sobrescribe pacientes.csv con la lista actual.
         *
         * @param pacientes lista a guardar.
         */
        public void guardarPacientes(List<Paciente> pacientes) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(pacientesFile, StandardCharsets.UTF_8))) {
                for (Paciente p : pacientes) {
                    bw.write(esc(p.getId()) + ";" + esc(p.getNombreCompleto()));
                    bw.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error escribiendo pacientes.csv: " + e.getMessage());
            }
        }

        /**
         * cargarCitas()
         * -------------
         * Lee citas.csv y construye lista de citas.
         * Importante: aquí NO validamos que doctorId/pacienteId existan, eso lo controla Sistema al crear citas.
         *
         * @return lista de citas cargadas.
         */
        public List<Cita> cargarCitas() {
            List<Cita> list = new ArrayList<>();
            if (!citasFile.exists()) return list;

            try (BufferedReader br = new BufferedReader(new FileReader(citasFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = splitSemi(line);
                    if (parts.length < 5) continue;

                    String id = unesc(parts[0]);
                    String fechaHora = unesc(parts[1]);
                    String motivo = unesc(parts[2]);
                    String doctorId = unesc(parts[3]);
                    String pacienteId = unesc(parts[4]);

                    list.add(new Cita(id, fechaHora, motivo, doctorId, pacienteId));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error leyendo citas.csv: " + e.getMessage());
            }
            return list;
        }

        /**
         * guardarCitas()
         * --------------
         * Sobrescribe citas.csv con todas las citas actuales.
         *
         * @param citas lista de citas a guardar.
         */
        public void guardarCitas(List<Cita> citas) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(citasFile, StandardCharsets.UTF_8))) {
                for (Cita c : citas) {
                    bw.write(esc(c.getId()) + ";" + esc(c.getFechaHora()) + ";" + esc(c.getMotivo())
                            + ";" + esc(c.getDoctorId()) + ";" + esc(c.getPacienteId()));
                    bw.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error escribiendo citas.csv: " + e.getMessage());
            }
        }

        /**
         * cargarUsuarios()
         * ----------------
         * Lee usuarios.csv y construye lista de usuarios.
         * Se carga el passwordHash directamente (ya viene calculado).
         *
         * @return lista de usuarios.
         */
        public List<Usuario> cargarUsuarios() {
            List<Usuario> list = new ArrayList<>();
            if (!usuariosFile.exists()) return list;

            try (BufferedReader br = new BufferedReader(new FileReader(usuariosFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = splitSemi(line);
                    if (parts.length < 4) continue;

                    String id = unesc(parts[0]);
                    String username = unesc(parts[1]);
                    String hash = unesc(parts[2]);
                    String rol = unesc(parts[3]);

                    list.add(new Usuario(id, username, hash, rol));
                }
            } catch (IOException e) {
                throw new RuntimeException("Error leyendo usuarios.csv: " + e.getMessage());
            }
            return list;
        }

        /**
         * guardarUsuarios()
         * -----------------
         * Sobrescribe usuarios.csv con la lista actual.
         *
         * @param usuarios lista de usuarios a guardar.
         */
        public void guardarUsuarios(List<Usuario> usuarios) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(usuariosFile, StandardCharsets.UTF_8))) {
                for (Usuario u : usuarios) {
                    bw.write(esc(u.getId()) + ";" + esc(u.getUsername()) + ";" + esc(u.getPasswordHash()) + ";" + esc(u.getRol()));
                    bw.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error escribiendo usuarios.csv: " + e.getMessage());
            }
        }

        /**
         * splitSemi()
         * -----------
         * Divide una línea por ';' PERO sin cortar cuando el ';' está escapado con '\;'.
         *
         * Ejemplo:
         * - Texto: "D1;Juan\\;Pérez;Cardiología"
         * - El nombre real contiene ';' => "Juan;Pérez"
         *
         * Aquí guardamos el texto con escapes, y luego unesc() se encarga de devolverlo normal.
         *
         * @param line línea completa del CSV
         * @return arreglo de "columnas" en texto escapado.
         */
        private String[] splitSemi(String line) {
            List<String> parts = new ArrayList<>();
            StringBuilder cur = new StringBuilder();
            boolean escaping = false;

            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);

                if (escaping) {
                    // Si el caracter anterior fue '\', metemos "\x" para que unesc() lo procese
                    cur.append('\\').append(ch);
                    escaping = false;
                    continue;
                }

                if (ch == '\\') {
                    escaping = true;
                    continue;
                }

                if (ch == ';') {
                    parts.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
            parts.add(cur.toString());
            return parts.toArray(new String[0]);
        }
    }

    // =======================
    // SISTEMA
    // =======================

    /**
     * CLASE Sistema
     * -------------
     * Es el “núcleo” del programa:
     * - Mantiene en memoria las listas de doctores, pacientes y citas.
     * - Tiene un UserManager para controlar acceso.
     * - Usa CsvStorage para guardar/cargar datos.
     *
     * Reglas importantes:
     * - Solo un ADMIN puede dar de alta doctores/pacientes y crear citas.
     * - IDs no se deben repetir dentro de su propia entidad.
     * - Al crear cita: doctorId y pacienteId deben existir.
     */
    static class Sistema {
        private List<Doctor> doctores = new ArrayList<>();
        private List<Paciente> pacientes = new ArrayList<>();
        private List<Cita> citas = new ArrayList<>();

        private final UserManager userManager = new UserManager();
        private final CsvStorage storage;

        /**
         * CONSTRUCTOR Sistema
         * -------------------
         * Recibe la carpeta de “db” y crea el almacenamiento.
         *
         * @param folderPath carpeta donde viven los archivos CSV.
         */
        public Sistema(String folderPath) {
            this.storage = new CsvStorage(folderPath);
        }

        /**
         * getUserManager()
         * ----------------
         * Permite acceder al control de usuarios desde main (para login o creación de admin por defecto).
         *
         * @return instancia de UserManager.
         */
        public UserManager getUserManager() { return userManager; }

        /**
         * cargarTodo()
         * ------------
         * Carga TODA la información desde CSV hacia memoria:
         * - doctores, pacientes, citas y usuarios.
         *
         * Nota:
         * - Cuando se cargan usuarios, se reinicia el usuarioActual por seguridad.
         */
        public void cargarTodo() {
            this.doctores = storage.cargarDoctores();
            this.pacientes = storage.cargarPacientes();
            this.citas = storage.cargarCitas();
            this.userManager.setUsuarios(storage.cargarUsuarios());
        }

        /**
         * guardarTodo()
         * -------------
         * Guarda TODA la información en CSV, sobrescribiendo los archivos con el estado actual.
         */
        public void guardarTodo() {
            storage.guardarDoctores(doctores);
            storage.guardarPacientes(pacientes);
            storage.guardarCitas(citas);
            storage.guardarUsuarios(userManager.getUsuarios());
        }

        /**
         * login()
         * -------
         * Atajo para llamar al UserManager.login().
         *
         * @param username usuario
         * @param password contraseña
         * @return true si login correcto.
         */
        public boolean login(String username, String password) {
            return userManager.login(username, password);
        }

        /**
         * altaDoctor()
         * ------------
         * Registra un doctor nuevo en el sistema.
         *
         * Validaciones:
         * - Debe ser ADMIN (requireAdmin).
         * - El ID del doctor no puede repetirse.
         *
         * @param d Doctor a registrar.
         */
        public void altaDoctor(Doctor d) {
            requireAdmin();

            if (buscarDoctorPorId(d.getId()) != null) {
                throw new RuntimeException("Doctor con id repetido");
            }
            doctores.add(d);
        }

        /**
         * altaPaciente()
         * --------------
         * Registra un paciente nuevo en el sistema.
         *
         * Validaciones:
         * - Debe ser ADMIN.
         * - El ID del paciente no puede repetirse.
         *
         * @param p Paciente a registrar.
         */
        public void altaPaciente(Paciente p) {
            requireAdmin();

            if (buscarPacientePorId(p.getId()) != null) {
                throw new RuntimeException("Paciente con id repetido");
            }
            pacientes.add(p);
        }

        /**
         * crearCita()
         * -----------
         * Crea una cita nueva y la agrega al sistema.
         *
         * Validaciones:
         * - Debe ser ADMIN.
         * - ID de cita no se repite.
         * - doctorId existe en doctores.
         * - pacienteId existe en pacientes.
         *
         * @param id ID único de la cita
         * @param fechaHora FechaHora en texto
         * @param motivo Motivo en texto
         * @param doctorId ID de doctor
         * @param pacienteId ID de paciente
         * @return La cita creada (por si se quiere imprimir info)
         */
        public Cita crearCita(String id, String fechaHora, String motivo, String doctorId, String pacienteId) {
            requireAdmin();

            for (Cita c : citas) {
                if (c.getId().equals(id)) {
                    throw new RuntimeException("Cita con id repetido");
                }
            }

            if (buscarDoctorPorId(doctorId) == null) {
                throw new RuntimeException("Doctor no existe");
            }
            if (buscarPacientePorId(pacienteId) == null) {
                throw new RuntimeException("Paciente no existe");
            }

            Cita nueva = new Cita(id, fechaHora, motivo, doctorId, pacienteId);
            citas.add(nueva);
            return nueva;
        }

        /**
         * buscarDoctorPorId()
         * -------------------
         * Busca un doctor por ID dentro de la lista en memoria.
         *
         * @param id ID del doctor
         * @return Doctor si existe, o null si no existe.
         */
        public Doctor buscarDoctorPorId(String id) {
            for (Doctor d : doctores) {
                if (d.getId().equals(id)) return d;
            }
            return null;
        }

        /**
         * buscarPacientePorId()
         * ---------------------
         * Busca un paciente por ID dentro de la lista en memoria.
         *
         * @param id ID del paciente
         * @return Paciente si existe, o null si no existe.
         */
        public Paciente buscarPacientePorId(String id) {
            for (Paciente p : pacientes) {
                if (p.getId().equals(id)) return p;
            }
            return null;
        }

        /**
         * requireAdmin()
         * --------------
         * Método interno para proteger operaciones administrativas.
         * Si no hay un ADMIN logueado, lanza excepción.
         *
         * Esto simplifica el resto del código, porque solo llamas requireAdmin() al inicio.
         */
        private void requireAdmin() {
            if (!userManager.esAdmin()) {
                throw new RuntimeException("Acceso denegado: se requiere ADMIN");
            }
        }
    }

    // =======================
    // MAIN (MENÚ)
    // =======================

    /**
     * main()
     * ------
     * Punto de entrada del programa.
     *
     * Flujo general:
     * 1) Crea el sistema y carga datos desde CSV.
     * 2) Si no hay usuarios, crea un admin por defecto (para demo).
     * 3) Pide login.
     * 4) Si el usuario es admin, muestra menú en loop.
     * 5) Permite crear doctores, pacientes, citas, guardar y salir.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Carpeta "db" donde se guardan los CSV (si no existe, se crea)
        Sistema sistema = new Sistema("db");
        sistema.cargarTodo();

        // Si es la primera vez que se ejecuta y no hay usuarios, creamos admin por defecto
        if (sistema.getUserManager().getUsuarios().isEmpty()) {
            sistema.getUserManager().registrarAdmin("A1", "admin", "admin123");
            sistema.guardarTodo();
            System.out.println("Se creó admin por defecto: usuario=admin, password=admin123");
        }

        // Login
        System.out.println("=== Login ===");
        System.out.print("Usuario: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        if (!sistema.login(username, password)) {
            System.out.println("Acceso denegado");
            return;
        }

        // Regla: solo admin puede operar el sistema
        if (!sistema.getUserManager().esAdmin()) {
            System.out.println("No tienes permisos de administrador");
            return;
        }

        // Menú principal
        while (true) {
            System.out.println();
            System.out.println("=== Sistema de Citas ===");
            System.out.println("1) Alta Doctor");
            System.out.println("2) Alta Paciente");
            System.out.println("3) Crear Cita");
            System.out.println("4) Guardar");
            System.out.println("5) Salir");
            System.out.print("Opción: ");

            int opcion = readInt(sc);

            try {
                switch (opcion) {

                    /**
                     * Opción 1: Alta de Doctor
                     * -ou
                     * Se piden datos por consola, se construye un Doctor y se manda al sistema.
                     */
                    case 1: {
                        System.out.print("ID Doctor: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre completo: ");
                        String nombre = sc.nextLine().trim();
                        System.out.print("Especialidad: ");
                        String esp = sc.nextLine().trim();

                        sistema.altaDoctor(new Doctor(id, nombre, esp));
                        System.out.println("Doctor registrado");
                        break;
                    }

                    /**
                     * Opción 2: Alta de Paciente
                     * Se piden datos por consola, se construye un Paciente y se guarda en memoria.
                     */
                    case 2: {
                        System.out.print("ID Paciente: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre completo: ");
                        String nombre = sc.nextLine().trim();

                        sistema.altaPaciente(new Paciente(id, nombre));
                        System.out.println("Paciente registrado");
                        break;
                    }

                    /**
                     * Opción 3: Crear Cita
                     * Se piden los datos de la cita y se valida que doctorId y pacienteId existan.
                     */
                    case 3: {
                        System.out.print("ID Cita: ");
                        String id = sc.nextLine().trim();
                        System.out.print("FechaHora (ISO-8601 recomendado): ");
                        String fechaHora = sc.nextLine().trim();
                        System.out.print("Motivo: ");
                        String motivo = sc.nextLine().trim();
                        System.out.print("DoctorId: ");
                        String doctorId = sc.nextLine().trim();
                        System.out.print("PacienteId: ");
                        String pacienteId = sc.nextLine().trim();

                        Cita cita = sistema.crearCita(id, fechaHora, motivo, doctorId, pacienteId);
                        System.out.println("Cita creada: " + cita.getId());
                        break;
                    }

                    /**
                     * Opción 4: Guardar
                     * Fuerza el guardado de todo a archivos CSV.
                     */
                    case 4:
                        sistema.guardarTodo();
                        System.out.println("Datos guardados en CSV");
                        break;

                    /**
                     * Opción 5: Salir
                     * Guarda antes de salir para no perder cambios.
                     */
                    case 5:
                        sistema.guardarTodo();
                        System.out.println("Saliendo...");
                        return;

                    /**
                     * Si el usuario mete un número no válido del menú.
                     */
                    default:
                        System.out.println("Opción inválida");
                }
            } catch (RuntimeException ex) {
                // Capturamos errores del sistema (IDs repetidos, doctor no existe, etc.)
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    // =======================
    // HELPERS
    // =======================

    /**
     * readInt()
     * ---------
     * Lee un número entero desde consola de forma segura.
     *
     * Problema común:
     * - Si el usuario escribe texto (ej: "hola") y hacemos Integer.parseInt, explota.
     *
     * Solución:
     * - Loop hasta que el usuario escriba un número válido.
     *
     * @param sc Scanner conectado a System.in
     * @return entero válido leído.
     */
    static int readInt(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("Ingresa un número válido: ");
            }
        }
    }

    /**
     * sha256()
     * --------
     * Calcula el hash SHA-256 de un texto.
     *
     * ¿Por qué se usa?
     * - Para no guardar contraseñas en texto plano.
     *
     * Flujo:
     * 1) MessageDigest aplica SHA-256 al texto.
     * 2) Se obtiene un arreglo de bytes.
     * 3) Se convierte a string hexadecimal (00..ff).
     *
     * @param text texto a hashear (ej: password)
     * @return hash en hex (string).
     */
    static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular SHA-256: " + e.getMessage());
        }
    }
}
