# INMIA — Estructura del Proyecto Android
> Java · API 34 · Groovy DSL · Navigation Component · MVVM (preparado, Firebase pendiente)

---

## Índice
1. [Visión general de la arquitectura](#1-visión-general-de-la-arquitectura)
2. [Estructura de paquetes Java](#2-estructura-de-paquetes-java)
3. [Estructura de recursos res/](#3-estructura-de-recursos-res)
4. [Navegación — NavGraphs](#4-navegación--navgraphs)
5. [Gradle — Dependencias](#5-gradle--dependencias)
6. [Esquema Firebase Firestore (referencia futura)](#6-esquema-firebase-firestore-referencia-futura)
7. [Convenciones de nombres anti-conflicto](#7-convenciones-de-nombres-anti-conflicto)
8. [Flujo de trabajo Git sugerido](#8-flujo-de-trabajo-git-sugerido)
9. [Tabla de responsables por archivo](#9-tabla-de-responsables-por-archivo)

---

## 1. Visión general de la arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                     CAPA DE UI                          │
│  Fragment / Activity  →  ViewModel  →  Repository       │
│  (cada rol tiene los suyos, no se tocan entre ellos)    │
└───────────────────────┬─────────────────────────────────┘
                        │ observa LiveData
┌───────────────────────▼─────────────────────────────────┐
│                  CAPA DE DATOS                          │
│  Repository  →  FirebaseHelper (placeholder por ahora)  │
│  Modelos compartidos en core/model/                     │
└─────────────────────────────────────────────────────────┘
```

**Fase actual:** UI hardcodeada (sin Firebase).
Los repositorios y el FirebaseHelper existen como clases vacías/placeholder
para que la estructura compile y los demás puedan ir completando sin refactorizar.

**Rol único de entrada:** Un solo login redirige a la interfaz del rol correspondiente.
La clase `RoleRouter` se encarga de esto (actualmente con rol hardcodeado para desarrollo).

---

## 2. Estructura de paquetes Java

```
com.example.inmia/
│
├── core/                          ← RESPONSABLE: SISTEMA
│   │
│   ├── model/                     ← Modelos de datos compartidos (TODOS los leen, SISTEMA los define)
│   │   ├── Usuario.java           ← Campos: id, nombres, apellidos, tipoDoc, numDoc, fechaNac,
│   │   │                              correo, telefono, domicilio, fotoUrl, rol, activo
│   │   ├── Inmobiliaria.java      ← Campos: id, nombre, adminId, ubicacion, correo, telefono,
│   │   │                              logoUrl, bannerUrl, fotosUrl[]
│   │   ├── Proyecto.java          ← Campos: id, inmobiliariaId, nombre, descripcion, ubicacion,
│   │   │                              estado(EstadoProyecto), precio, precioSeparacion,
│   │   │                              tipologia[], areasComunes[], imagenesUrl[],
│   │   │                              fechaEntregaEstimada, codigoQR
│   │   ├── Cita.java              ← Campos: id, clienteId, asesorId, proyectoId, fecha, hora,
│   │   │                              estado(EstadoCita), notas
│   │   ├── Separacion.java        ← Campos: id, clienteId, asesorId, proyectoId, monto,
│   │   │                              estado(EstadoSeparacion), fechaCreacion, fechaLimite,
│   │   │                              valoracionCliente, observacionesCliente
│   │   ├── Notificacion.java      ← Campos: id, usuarioId, tipo, titulo, mensaje, leida, fecha
│   │   ├── Mensaje.java           ← Campos: id, chatId, remitenteId, contenido, fecha
│   │   ├── Chat.java              ← Campos: id, inmobiliariaId, participantes[], ultimoMensaje
│   │   └── enums/
│   │       ├── RolUsuario.java         ← CLIENTE, ASESOR, ADMIN_INMOBILIARIA, SUPERADMIN
│   │       ├── EstadoProyecto.java     ← EN_PLANOS, EN_PREVENTA, EN_VENTA
│   │       ├── EstadoCita.java         ← PENDIENTE, CONFIRMADA, CANCELADA, EN_PROCESO
│   │       └── EstadoSeparacion.java   ← POR_CONFIRMAR, APROBADA, NO_APROBADA, PAGADA
│   │
│   ├── repository/                ← Placeholder Firebase — NO MODIFICAR AÚN
│   │   ├── AuthRepository.java    ← Placeholder: login(), register(), logout()
│   │   ├── UsuarioRepository.java ← Placeholder: getUsuario(), updateUsuario()
│   │   ├── ProyectoRepository.java← Placeholder: getProyectos(), getProyectoById()
│   │   ├── CitaRepository.java    ← Placeholder: getCitas(), crearCita(), cancelarCita()
│   │   ├── SeparacionRepository.java ← Placeholder: getSeparaciones(), crearSeparacion()
│   │   ├── ChatRepository.java    ← Placeholder: getChats(), getMensajes(), enviarMensaje()
│   │   └── NotificacionRepository.java ← Placeholder: getNotificaciones(), marcarLeida()
│   │
│   ├── util/
│   │   ├── RoleRouter.java        ← Lee el rol del usuario y navega al NavGraph correcto.
│   │   │                              Por ahora: rol hardcodeado como constante para testing.
│   │   │                              Futuro: leerá Firestore.
│   │   ├── SessionManager.java    ← Guarda en SharedPreferences: userId, rol, token (placeholder).
│   │   │                              Métodos: saveSession(), getSession(), clearSession(), isLoggedIn()
│   │   └── Constants.java         ← Constantes globales: TIEMPO_PAGO_MS, ROLES, COLECCIONES_FIRESTORE
│   │
│   └── ui/
│       ├── base/
│       │   ├── BaseActivity.java  ← Extiende AppCompatActivity. Métodos comunes: showLoading(),
│       │   │                          hideLoading(), showToast(), checkNetworkConnection()
│       │   └── BaseFragment.java  ← Extiende Fragment. Mismos métodos utilitarios. Todos los
│       │                              fragments del proyecto extienden de este.
│       │
│       ├── splash/
│       │   └── SplashFragment.java ← Pantalla inicial con logo INMIA. Espera 1.5s y navega
│       │                               a OnboardingFragment (primera vez) o LoginFragment.
│       │                               Usa SessionManager para detectar si ya hay sesión activa.
│       │
│       ├── onboarding/
│       │   └── OnboardingFragment.java ← 3 slides informativos ("Descubre tu próximo hogar",
│       │                                   "Compara y elige mejor", "Hazlo tuyo").
│       │                                   Usa ViewPager2 + TabLayout para los dots.
│       │                                   Solo se muestra en el primer arranque.
│       │
│       ├── auth/
│       │   ├── LoginFragment.java          ← Campos: correo, contraseña. Check "Recuérdame".
│       │   │                                   Link a ForgotPasswordFragment y RegisterSelectionFragment.
│       │   │                                   Al ingresar llama a RoleRouter para redirigir.
│       │   ├── RegisterSelectionFragment.java ← Pantalla intermedia: ¿Eres cliente o asesor?
│       │   │                                   (Los admins los registra el superadmin directamente)
│       │   ├── RegisterClienteFragment.java ← Formulario completo: nombres, apellidos, tipoDoc,
│       │   │                                   numDoc, fechaNac, correo, telefono, domicilio.
│       │   │                                   Navega a RegisterFotoFragment al terminar.
│       │   ├── RegisterAsesorFragment.java  ← Mismo formulario que cliente + campo inmobiliaria destino.
│       │   │                                   Al registrar queda en estado "pendiente aprobación".
│       │   ├── RegisterFotoFragment.java    ← Sube foto de perfil. Opciones: galería o cámara.
│       │   │                                   Pantalla final del registro.
│       │   ├── ForgotPasswordFragment.java  ← Ingresa correo para recuperar acceso.
│       │   ├── NewPasswordFragment.java     ← Ingresa nueva contraseña + confirmación.
│       │   └── AccountDisabledFragment.java ← Pantalla informativa cuando la cuenta está inhabilitada.
│       │                                       Solo muestra mensaje y botón para volver al login.
│       │
│       ├── chat/
│       │   ├── ChatListFragment.java   ← Lista de conversaciones activas del usuario logueado.
│       │   │                               Muestra: foto, nombre, último mensaje, hora, badge no leídos.
│       │   │                               RecyclerView con adapter item_sistema_chat_preview.xml
│       │   └── ChatDetailFragment.java ← Conversación individual. Mensajes del asesor y cliente.
│       │                                   Header muestra el proyecto vinculado al chat.
│       │                                   Input de texto + botón enviar.
│       │
│       └── notifications/
│           └── NotificationsFragment.java ← Lista de notificaciones del usuario logueado.
│                                               Tipos: cita confirmada, separación solicitada/aprobada,
│                                               cita cancelada, pago recibido. Cada item con ícono por tipo.
│
│
├── cliente/                       ← RESPONSABLE: INTEGRANTE CLIENTE
│   │
│   ├── ui/
│   │   ├── home/
│   │   │   └── ClienteHomeFragment.java    ← Pantalla principal. Header "Encuentra tu hogar ideal".
│   │   │                                       Barra de búsqueda por zona/proyecto/distrito.
│   │   │                                       Filtros: Todos / Preventa / Planos / Venta.
│   │   │                                       RecyclerView de proyectos destacados (cards grandes).
│   │   │                                       BottomNav: Home, Citas, Mensajes, Separaciones.
│   │   │
│   │   ├── explore/
│   │   │   └── ClienteExploreFragment.java ← Lista completa de proyectos con filtros activos.
│   │   │                                       RecyclerView vertical. Cada item muestra imagen,
│   │   │                                       nombre, ubicación, precio, estado, rating.
│   │   │
│   │   ├── project_detail/
│   │   │   ├── ClienteProjectDetailFragment.java ← Detalle completo: galería de imágenes (ViewPager2),
│   │   │   │                                         nombre inmobiliaria (badge), estado proyecto,
│   │   │   │                                         nombre, rating, ubicación, descripción,
│   │   │   │                                         tipología, áreas comunes, precio, botones
│   │   │   │                                         "Agendar cita" y "Ver en mapa".
│   │   │   ├── ClienteProjectMapFragment.java    ← Google Maps con marcador del proyecto y
│   │   │   │                                         puntos de interés cercanos. PENDIENTE (Maps SDK).
│   │   │   ├── ClienteProjectReviewsFragment.java ← Lista de reseñas con rating individual,
│   │   │   │                                          nombre reviewer, comentario.
│   │   │   │                                          Gráfico de barras de distribución de estrellas.
│   │   │   └── ClienteProjectQRFragment.java     ← Escáner QR que redirige al proyecto.
│   │   │                                             PENDIENTE (ML Kit / ZXing).
│   │   │
│   │   ├── citas/
│   │   │   ├── ClienteCitasFragment.java       ← Lista de citas del cliente. Estados:
│   │   │   │                                       confirmada, pendiente, cancelada, en proceso.
│   │   │   │                                       RecyclerView con tabs por estado.
│   │   │   ├── ClienteAgendarCitaFragment.java ← Formulario: proyecto pre-seleccionado,
│   │   │   │                                       selector de fecha (DatePicker), selector hora.
│   │   │   │                                       Validación: no permite superposición de horarios.
│   │   │   └── ClienteCitaDetailFragment.java  ← Detalle: proyecto, asesor asignado, fecha/hora,
│   │   │                                           ubicación (mapa miniatura), botón cancelar.
│   │   │
│   │   ├── separaciones/
│   │   │   ├── ClienteSeparacionesFragment.java     ← Lista de separaciones. Tabs por estado:
│   │   │   │                                             En espera / Aprobada / No aprobada.
│   │   │   │                                             Botón "Pagar" visible solo en Aprobadas.
│   │   │   └── ClienteSeparacionDetailFragment.java ← Detalle: proyecto, inmobiliaria, monto,
│   │   │                                                 fecha límite, countdown 10 min (pendiente),
│   │   │                                                 botón pagar (requiere tarjeta registrada).
│   │   │
│   │   ├── profile/
│   │   │   ├── ClienteProfileFragment.java     ← Muestra foto, nombre, rol "Cliente".
│   │   │   │                                       Opciones: Información Personal, Métodos de Pago,
│   │   │   │                                       Seguridad, Notificaciones, Idioma, Cerrar sesión.
│   │   │   ├── ClienteEditProfileFragment.java ← Editar: nombres, apellidos, ubicación,
│   │   │   │                                       correo, teléfono, foto.
│   │   │   └── ClienteSettingsFragment.java    ← Idioma (spinner), Notificaciones (toggles por tipo),
│   │   │                                           Seguridad (Face ID, Touch ID, recordar contraseña).
│   │   │
│   │   └── payment/
│   │       └── ClientePaymentCardsFragment.java ← Lista de tarjetas registradas. Muestra balance,
│   │                                                  número enmascarado, fecha expiración.
│   │                                                  Opción "Usar como predeterminada".
│   │                                                  PENDIENTE (integración de pago real).
│   │
│   └── viewmodel/
│       ├── ClienteHomeViewModel.java       ← LiveData<List<Proyecto>> proyectos. Filtro por estado.
│       ├── ClienteCitasViewModel.java      ← LiveData<List<Cita>> citas. Lógica validación horarios.
│       ├── ClienteSeparacionesViewModel.java ← LiveData<List<Separacion>> separaciones.
│       └── ClienteProfileViewModel.java   ← LiveData<Usuario> perfil.
│
│
├── asesor/                        ← RESPONSABLE: INTEGRANTE ASESOR
│   │
│   ├── ui/
│   │   ├── home/
│   │   │   └── AsesorHomeFragment.java         ← Dashboard del asesor. Muestra nombre inmobiliaria,
│   │   │                                             contador de citas del día, accesos rápidos:
│   │   │                                             Mis citas, Mis proyectos, Registrar separación.
│   │   │                                             BottomNav: Home, Citas, Separaciones, Perfil.
│   │   │
│   │   ├── citas/
│   │   │   ├── AsesorCitasFragment.java        ← Lista de citas agendadas por clientes para
│   │   │   │                                       sus proyectos asignados. Tabs: Pendientes / Historial.
│   │   │   └── AsesorCitaDetailFragment.java   ← Detalle: cliente, proyecto, fecha/hora,
│   │   │                                           datos de contacto del cliente.
│   │   │
│   │   ├── separaciones/
│   │   │   ├── AsesorSeparacionesFragment.java         ← Lista de separaciones registradas por este asesor.
│   │   │   │                                               Estados: por confirmar, aprobada, no aprobada.
│   │   │   └── AsesorRegistrarSeparacionFragment.java  ← Flujo de 3 pasos (Stepper manual):
│   │   │       │                                           Paso 1: buscar cliente por nombre/DNI.
│   │   │       │                                           Paso 2: seleccionar proyecto de su lista asignada.
│   │   │       │                                           Paso 3: ingresar monto, fecha límite, condiciones.
│   │   │       └── (Stepper implementado dentro del mismo Fragment con visibilidad de vistas)
│   │   │
│   │   ├── proyectos/
│   │   │   └── AsesorProyectosFragment.java    ← Lista de proyectos asignados a este asesor.
│   │   │                                           Cada item muestra nombre, estado, inmobiliaria.
│   │   │
│   │   └── profile/
│   │       ├── AsesorProfileFragment.java      ← Foto, nombre, rol "Asesor de Ventas", inmobiliaria.
│   │       │                                       Opciones: Información Personal, Cerrar sesión.
│   │       └── AsesorEditProfileFragment.java  ← Editar campos personales del asesor.
│   │
│   └── viewmodel/
│       ├── AsesorCitasViewModel.java           ← LiveData<List<Cita>> citas del asesor.
│       ├── AsesorSeparacionesViewModel.java    ← LiveData<List<Separacion>> separaciones.
│       ├── AsesorProyectosViewModel.java       ← LiveData<List<Proyecto>> proyectos asignados.
│       └── AsesorProfileViewModel.java         ← LiveData<Usuario> perfil asesor.
│
│
├── admin/                         ← RESPONSABLE: INTEGRANTE ADMIN INMOBILIARIA
│   │
│   ├── ui/
│   │   ├── home/
│   │   │   └── AdminHomeFragment.java              ← Dashboard: nombre inmobiliaria, contadores
│   │   │                                               (proyectos activos, asesores, separaciones hoy).
│   │   │                                               Accesos rápidos. BottomNav: Home, Proyectos,
│   │   │                                               Asesores, Separaciones, Perfil.
│   │   │
│   │   ├── inmobiliaria/
│   │   │   ├── AdminInmobiliariaInfoFragment.java  ← Ver/editar info de la empresa: nombre, ubicación,
│   │   │   │                                           correo, teléfono.
│   │   │   └── AdminSubirFotosFragment.java        ← Subir logo y banner de la inmobiliaria.
│   │   │                                               Mínimo 2 fotos promocionales (validado).
│   │   │
│   │   ├── proyectos/
│   │   │   ├── AdminProyectosFragment.java         ← Lista de proyectos de la inmobiliaria.
│   │   │   │                                           Filtro por estado. Botón "Nuevo proyecto".
│   │   │   ├── AdminCrearEditarProyectoFragment.java ← Formulario: nombre, descripción, ubicación
│   │   │   │                                            (mapa picker pendiente), estado (spinner),
│   │   │   │                                            tipología, áreas comunes (checkboxes),
│   │   │   │                                            precio total, precio separación,
│   │   │   │                                            fecha estimada entrega, imágenes (mín. 2).
│   │   │   │                                            Sirve para crear Y editar (un flag isEditing).
│   │   │   └── AdminProyectoDetailFragment.java    ← Vista detallada del proyecto con botones
│   │   │                                               editar, ver asesores asignados, generar QR (pendiente).
│   │   │
│   │   ├── asesores/
│   │   │   ├── AdminAsesoresFragment.java          ← Lista de asesores de la inmobiliaria.
│   │   │   │                                           Tabs: Activos / Solicitudes pendientes.
│   │   │   │                                           Botón para habilitar/deshabilitar.
│   │   │   ├── AdminAsesorDetailFragment.java      ← Info completa del asesor: datos, proyectos
│   │   │   │                                           asignados, historial de separaciones.
│   │   │   └── AdminAsignarAsesorFragment.java     ← Asignar un asesor a uno o más proyectos.
│   │   │                                               Lista de proyectos con checkboxes.
│   │   │
│   │   ├── separaciones/
│   │   │   ├── AdminSeparacionesFragment.java      ← Lista global de separaciones de la inmobiliaria.
│   │   │   │                                           Filtro por proyecto y por asesor.
│   │   │   │                                           Tabs: Por confirmar / Aprobadas / Rechazadas.
│   │   │   └── AdminSeparacionDetailFragment.java  ← Detalle con datos del cliente, asesor, proyecto,
│   │   │                                               monto propuesto. Botones: Aprobar / Rechazar.
│   │   │
│   │   ├── reportes/
│   │   │   └── AdminReportesFragment.java          ← Gráficos de separaciones/ventas.
│   │   │                                               Filtros: diario / mensual / anual.
│   │   │                                               Desglose por proyecto y por asesor.
│   │   │                                               PENDIENTE (MPAndroidChart).
│   │   │
│   │   └── profile/
│   │       └── AdminProfileFragment.java           ← Perfil del administrador. Cerrar sesión.
│   │
│   └── viewmodel/
│       ├── AdminProyectosViewModel.java        ← LiveData<List<Proyecto>> proyectos de la inmobiliaria.
│       ├── AdminAsesoresViewModel.java         ← LiveData<List<Usuario>> asesores.
│       ├── AdminSeparacionesViewModel.java     ← LiveData<List<Separacion>> con lógica aprobación.
│       └── AdminReportesViewModel.java         ← LiveData con datos agregados para gráficos.
│
│
└── superadmin/                    ← RESPONSABLE: INTEGRANTE SUPERADMIN
    │
    ├── ui/
    │   ├── home/
    │   │   └── SuperadminHomeFragment.java         ← Dashboard global: total usuarios, inmobiliarias,
    │   │                                               separaciones activas. BottomNav: Reportes,
    │   │                                               Usuarios, Logs, Perfil.
    │   │
    │   ├── usuarios/
    │   │   ├── SuperadminUsuariosFragment.java     ← Tabs: Admins / Asesores / Clientes.
    │   │   │                                           Cada tab carga su fragment hijo.
    │   │   │                                           Barra de búsqueda global.
    │   │   ├── SuperadminAdminsFragment.java       ← Lista admins con toggle activo/inactivo.
    │   │   │                                           Botón "+ Nuevo" para crear admin.
    │   │   ├── SuperadminCrearAdminFragment.java   ← Formulario: mismos campos de Usuario.
    │   │   │                                           Asignar a una inmobiliaria.
    │   │   ├── SuperadminAsesoresFragment.java     ← Lista asesores. Sub-tabs: Activos / Solicitudes.
    │   │   │                                           Ver solicitudes pendientes de habilitación.
    │   │   ├── SuperadminClientesFragment.java     ← Lista clientes con toggle activo/inactivo.
    │   │   ├── SuperadminUserDetailFragment.java   ← Perfil completo de cualquier usuario.
    │   │   │                                           Toggle habilitar/deshabilitar. Datos completos.
    │   │   └── SuperadminAsesorSolicitudFragment.java ← Detalle solicitud de asesor: datos, inmobiliaria
    │   │                                                  destino. Botones: Aceptar / Rechazar.
    │   │
    │   ├── logs/
    │   │   ├── SuperadminLogsFragment.java         ← Lista de eventos del sistema (paginada).
    │   │   │                                           Cada evento con ícono tipo, descripción, fecha/hora.
    │   │   │                                           Barra de filtro por fecha (DateRangePicker).
    │   │   └── SuperadminLogsFilterFragment.java   ← BottomSheet para seleccionar rango de fechas.
    │   │
    │   ├── reportes/
    │   │   └── SuperadminReportesFragment.java     ← Reporte global de reservas por empresa.
    │   │                                               Gráficos comparativos entre inmobiliarias.
    │   │                                               PENDIENTE (MPAndroidChart).
    │   │
    │   └── profile/
    │       └── SuperadminProfileFragment.java      ← Perfil superadmin. Cerrar sesión.
    │
    └── viewmodel/
        ├── SuperadminUsuariosViewModel.java    ← LiveData con listas de cada tipo de usuario.
        ├── SuperadminLogsViewModel.java        ← LiveData<List<LogEvento>> con filtros de fecha.
        └── SuperadminReportesViewModel.java    ← LiveData con datos globales agregados.
```

---

## 3. Estructura de recursos `res/`

```
res/
│
├── drawable/                      ← Imágenes y vectores (sin conflictos, archivos separados)
│   ├── ic_launcher_background.xml     (generado, no tocar)
│   ├── ic_launcher_foreground.xml     (generado, no tocar)
│   │
│   │  ── Compartidos (SISTEMA define, todos usan) ──
│   ├── shared_ic_home.xml
│   ├── shared_ic_chat.xml
│   ├── shared_ic_notifications.xml
│   ├── shared_ic_profile.xml
│   ├── shared_ic_back_arrow.xml
│   ├── shared_ic_search.xml
│   ├── shared_bg_card_rounded.xml     ← Background para cards (shape)
│   ├── shared_bg_button_primary.xml   ← Background botón principal (teal)
│   ├── shared_bg_button_outline.xml   ← Background botón outline
│   ├── shared_bg_input_field.xml      ← Background campos de texto
│   │
│   │  ── Sistema ──
│   ├── sistema_logo_inmia.xml         ← Logo principal (vector)
│   ├── sistema_bg_splash_gradient.xml ← Gradiente teal del splash
│   ├── sistema_ic_eye_toggle.xml
│   ├── sistema_onboarding_slide1.xml
│   ├── sistema_onboarding_slide2.xml
│   ├── sistema_onboarding_slide3.xml
│   │
│   │  ── Cliente ──
│   ├── cliente_ic_citas.xml
│   ├── cliente_ic_separaciones.xml
│   ├── cliente_ic_payment_card.xml
│   ├── cliente_badge_en_planos.xml    ← Chip verde "En planos"
│   ├── cliente_badge_en_preventa.xml  ← Chip naranja "En preventa"
│   └── cliente_badge_en_venta.xml     ← Chip azul "En venta"
│   │
│   │  ── Asesor ──
│   ├── asesor_ic_citas.xml
│   └── asesor_ic_separacion_nueva.xml
│   │
│   │  ── Admin ──
│   ├── admin_ic_proyectos.xml
│   ├── admin_ic_asesores.xml
│   └── admin_ic_reportes.xml
│   │
│   │  ── Superadmin ──
│   ├── superadmin_ic_usuarios.xml
│   └── superadmin_ic_logs.xml
│
│
├── layout/                        ← Un archivo XML por pantalla (sin conflictos directos)
│   │
│   │  ── App shell ──
│   ├── activity_main.xml          ← Solo contiene NavHostFragment. SISTEMA.
│   │
│   │  ── Sistema ──
│   ├── sistema_fragment_splash.xml
│   ├── sistema_fragment_onboarding.xml
│   ├── sistema_fragment_login.xml
│   ├── sistema_fragment_register_selection.xml
│   ├── sistema_fragment_register_cliente.xml
│   ├── sistema_fragment_register_asesor.xml
│   ├── sistema_fragment_register_foto.xml
│   ├── sistema_fragment_forgot_password.xml
│   ├── sistema_fragment_new_password.xml
│   ├── sistema_fragment_account_disabled.xml
│   ├── sistema_fragment_chat_list.xml
│   ├── sistema_fragment_chat_detail.xml
│   ├── sistema_fragment_notifications.xml
│   ├── item_sistema_chat_preview.xml      ← Item RecyclerView lista de chats
│   ├── item_sistema_notification.xml      ← Item RecyclerView notificaciones
│   │
│   │  ── Cliente ──
│   ├── cliente_fragment_home.xml
│   ├── cliente_fragment_explore.xml
│   ├── cliente_fragment_project_detail.xml
│   ├── cliente_fragment_project_map.xml
│   ├── cliente_fragment_project_reviews.xml
│   ├── cliente_fragment_qr_scanner.xml
│   ├── cliente_fragment_citas.xml
│   ├── cliente_fragment_agendar_cita.xml
│   ├── cliente_fragment_cita_detail.xml
│   ├── cliente_fragment_separaciones.xml
│   ├── cliente_fragment_separacion_detail.xml
│   ├── cliente_fragment_profile.xml
│   ├── cliente_fragment_edit_profile.xml
│   ├── cliente_fragment_settings.xml
│   ├── cliente_fragment_payment_cards.xml
│   ├── item_cliente_proyecto_card.xml     ← Item RecyclerView proyectos
│   ├── item_cliente_cita.xml
│   ├── item_cliente_separacion.xml
│   └── item_cliente_review.xml
│   │
│   │  ── Asesor ──
│   ├── asesor_fragment_home.xml
│   ├── asesor_fragment_citas.xml
│   ├── asesor_fragment_cita_detail.xml
│   ├── asesor_fragment_separaciones.xml
│   ├── asesor_fragment_registrar_separacion.xml
│   ├── asesor_fragment_proyectos.xml
│   ├── asesor_fragment_profile.xml
│   ├── asesor_fragment_edit_profile.xml
│   ├── item_asesor_cita.xml
│   └── item_asesor_separacion.xml
│   │
│   │  ── Admin ──
│   ├── admin_fragment_home.xml
│   ├── admin_fragment_inmobiliaria_info.xml
│   ├── admin_fragment_subir_fotos.xml
│   ├── admin_fragment_proyectos.xml
│   ├── admin_fragment_crear_editar_proyecto.xml
│   ├── admin_fragment_proyecto_detail.xml
│   ├── admin_fragment_asesores.xml
│   ├── admin_fragment_asesor_detail.xml
│   ├── admin_fragment_asignar_asesor.xml
│   ├── admin_fragment_separaciones.xml
│   ├── admin_fragment_separacion_detail.xml
│   ├── admin_fragment_reportes.xml
│   ├── admin_fragment_profile.xml
│   ├── item_admin_proyecto.xml
│   ├── item_admin_asesor.xml
│   └── item_admin_separacion.xml
│   │
│   │  ── Superadmin ──
│   ├── superadmin_fragment_home.xml
│   ├── superadmin_fragment_usuarios.xml
│   ├── superadmin_fragment_admins.xml
│   ├── superadmin_fragment_crear_admin.xml
│   ├── superadmin_fragment_asesores.xml
│   ├── superadmin_fragment_clientes.xml
│   ├── superadmin_fragment_user_detail.xml
│   ├── superadmin_fragment_asesor_solicitud.xml
│   ├── superadmin_fragment_logs.xml
│   ├── superadmin_fragment_logs_filter.xml
│   ├── superadmin_fragment_reportes.xml
│   ├── superadmin_fragment_profile.xml
│   ├── item_superadmin_usuario.xml
│   └── item_superadmin_log.xml
│
│
├── navigation/                    ← NavGraphs separados (sin conflictos entre roles)
│   ├── nav_graph_root.xml         ← SISTEMA: grafo raíz. Solo incluye los grafos anidados.
│   │                                  startDestination = sistema_splash
│   ├── nav_graph_sistema.xml      ← SISTEMA: splash, onboarding, auth (login, register, etc.)
│   ├── nav_graph_cliente.xml      ← CLIENTE: todas las pantallas del rol cliente
│   ├── nav_graph_asesor.xml       ← ASESOR: todas las pantallas del rol asesor
│   ├── nav_graph_admin.xml        ← ADMIN: todas las pantallas del rol admin inmobiliaria
│   └── nav_graph_superadmin.xml   ← SUPERADMIN: todas las pantallas del rol superadmin
│
│
└── values/                        ← Archivos separados por rol (Android los fusiona automáticamente)
    │
    ├── colors.xml                 ← SISTEMA define y mantiene. Paleta global INMIA.
    │                                  Nadie más edita este archivo.
    │   Contenido:
    │     primary_teal      = #00BFA5
    │     primary_teal_dark  = #007A65
    │     background_light  = #F5FBFA
    │     text_primary      = #1A1A2E
    │     text_secondary    = #6B7280
    │     status_success    = #22C55E
    │     status_warning    = #F59E0B
    │     status_error      = #EF4444
    │     status_info       = #3B82F6
    │     badge_planos      = #A8D8A8
    │     badge_preventa    = #FBBF72
    │     badge_venta       = #93C5FD
    │
    ├── themes.xml                 ← SISTEMA define. Tema Material3 con colores INMIA.
    │                                  Nadie más edita.
    │
    ├── strings.xml                ← SISTEMA: strings compartidos y del módulo sistema.
    │   Contenido (ejemplos):
    │     app_name, sistema_btn_ingresar, sistema_hint_correo,
    │     sistema_hint_contrasena, sistema_link_olvidaste,
    │     sistema_msg_cuenta_inhabilitada, shared_btn_cancelar,
    │     shared_btn_confirmar, shared_btn_guardar, shared_btn_volver
    │
    ├── strings_cliente.xml        ← CLIENTE: solo sus strings. No tocar los demás.
    │   Ejemplos: cliente_title_home, cliente_label_precio_desde, cliente_btn_agendar
    │
    ├── strings_asesor.xml         ← ASESOR: solo sus strings.
    │   Ejemplos: asesor_title_citas, asesor_btn_registrar_separacion
    │
    ├── strings_admin.xml          ← ADMIN: solo sus strings.
    │   Ejemplos: admin_title_proyectos, admin_btn_nuevo_proyecto, admin_btn_aprobar
    │
    ├── strings_superadmin.xml     ← SUPERADMIN: solo sus strings.
    │   Ejemplos: superadmin_title_logs, superadmin_btn_habilitar
    │
    └── dimens.xml                 ← SISTEMA define. Márgenes y tamaños comunes.
        Contenido: margin_standard=16dp, margin_small=8dp,
                   card_corner_radius=16dp, button_height=52dp
```

---

## 4. Navegación — NavGraphs

### Flujo general
```
app launch
    │
    ▼
[SplashFragment]  ──── primera vez ────▶ [OnboardingFragment] ──▶ [LoginFragment]
    │                                                                     │
    └──── sesión activa ──── RoleRouter ──────────────────────────────────┤
                                │                                         │
                                │◀────────────────── login exitoso ───────┘
                                │
         ┌──────────────────────┼──────────────────────────┐
         │                      │                           │
         ▼                      ▼                           ▼
 [nav_cliente]          [nav_asesor]                [nav_admin]
         │
         ▼
 [nav_superadmin]
```

### nav_graph_root.xml (SISTEMA)
```xml
<!-- startDestination: nav_graph_sistema -->
<!-- include: nav_graph_sistema, nav_graph_cliente,
              nav_graph_asesor, nav_graph_admin, nav_graph_superadmin -->
<!-- Acciones globales: a cada grafo anidado por rol -->
```

### nav_graph_sistema.xml (SISTEMA)
```
SplashFragment → OnboardingFragment → LoginFragment
LoginFragment → ForgotPasswordFragment → NewPasswordFragment
LoginFragment → RegisterSelectionFragment → RegisterClienteFragment → RegisterFotoFragment
                                          → RegisterAsesorFragment  → RegisterFotoFragment
LoginFragment → AccountDisabledFragment
```

### nav_graph_cliente.xml (CLIENTE)
```
ClienteHomeFragment (startDestination)
  → ClienteExploreFragment
  → ClienteProjectDetailFragment
      → ClienteProjectMapFragment
      → ClienteProjectReviewsFragment
      → ClienteProjectQRFragment
      → ClienteAgendarCitaFragment
  → ClienteCitasFragment → ClienteCitaDetailFragment
  → ClienteSeparacionesFragment → ClienteSeparacionDetailFragment
  → ClienteProfileFragment → ClienteEditProfileFragment
                           → ClienteSettingsFragment
                           → ClientePaymentCardsFragment
  → [global] ChatListFragment → ChatDetailFragment
  → [global] NotificationsFragment
```

### nav_graph_asesor.xml (ASESOR)
```
AsesorHomeFragment (startDestination)
  → AsesorCitasFragment → AsesorCitaDetailFragment
  → AsesorSeparacionesFragment → AsesorRegistrarSeparacionFragment
  → AsesorProyectosFragment
  → AsesorProfileFragment → AsesorEditProfileFragment
  → [global] ChatListFragment → ChatDetailFragment
  → [global] NotificationsFragment
```

### nav_graph_admin.xml (ADMIN)
```
AdminHomeFragment (startDestination)
  → AdminInmobiliariaInfoFragment → AdminSubirFotosFragment
  → AdminProyectosFragment → AdminCrearEditarProyectoFragment
                           → AdminProyectoDetailFragment
  → AdminAsesoresFragment  → AdminAsesorDetailFragment
                           → AdminAsignarAsesorFragment
  → AdminSeparacionesFragment → AdminSeparacionDetailFragment
  → AdminReportesFragment
  → AdminProfileFragment
  → [global] NotificationsFragment
```

### nav_graph_superadmin.xml (SUPERADMIN)
```
SuperadminHomeFragment (startDestination)
  → SuperadminUsuariosFragment → SuperadminAdminsFragment
  │                            → SuperadminCrearAdminFragment
  │                            → SuperadminAsesoresFragment
  │                            → SuperadminClientesFragment
  │                            → SuperadminUserDetailFragment
  │                            → SuperadminAsesorSolicitudFragment
  → SuperadminLogsFragment → SuperadminLogsFilterFragment (BottomSheet)
  → SuperadminReportesFragment
  → SuperadminProfileFragment
  → [global] NotificationsFragment
```

---

## 5. Gradle — Dependencias

### `build.gradle` (Module :app)

```groovy
android {
    compileSdk 34
    defaultConfig {
        applicationId "com.example.inmia"
        minSdk 34
        targetSdk 34
    }
}

dependencies {
    // ── Core Android (activas) ──────────────────────────────────────
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // ── Navigation Component (activa) ───────────────────────────────
    implementation 'androidx.navigation:navigation-fragment:2.7.7'
    implementation 'androidx.navigation:navigation-ui:2.7.7'

    // ── ViewModel + LiveData (activa) ───────────────────────────────
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.8.3'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.8.3'

    // ── RecyclerView (activa) ───────────────────────────────────────
    implementation 'androidx.recyclerview:recyclerview:1.3.2'

    // ── ViewPager2 (activa — onboarding, galería proyectos) ─────────
    implementation 'androidx.viewpager2:viewpager2:1.1.0'

    // ── Glide — carga de imágenes (activa desde fase mockups) ───────
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

    // ── SharedPreferences (SessionManager) — ya incluido en core ────

    // ── Firebase — PENDIENTE (habilitar en Lab 6) ───────────────────
    // implementation platform('com.google.firebase:firebase-bom:33.1.2')
    // implementation 'com.google.firebase:firebase-auth'
    // implementation 'com.google.firebase:firebase-firestore'
    // implementation 'com.google.firebase:firebase-messaging'
    // implementation 'com.google.firebase:firebase-storage'

    // ── Google Maps — PENDIENTE (habilitar en Lab 3 o Lab 4) ────────
    // implementation 'com.google.android.gms:play-services-maps:19.0.0'
    // implementation 'com.google.android.gms:play-services-location:21.3.0'

    // ── QR Scanner (ML Kit) — PENDIENTE ─────────────────────────────
    // implementation 'com.google.mlkit:barcode-scanning:17.3.0'
    // implementation 'androidx.camera:camera-camera2:1.3.4'
    // implementation 'androidx.camera:camera-lifecycle:1.3.4'
    // implementation 'androidx.camera:camera-view:1.3.4'

    // ── Reportes / Gráficos — PENDIENTE (Lab 5 o Lab 6) ─────────────
    // implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

    // ── Tests ────────────────────────────────────────────────────────
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation 'androidx.espresso:espresso-core:3.6.1'
}
```

### `settings.gradle` — agregar si se usa MPAndroidChart

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Descomentar si usan MPAndroidChart:
        // maven { url 'https://jitpack.io' }
    }
}
```

---

## 6. Esquema Firebase Firestore (referencia futura)

> **No implementar aún.** Este esquema define la estructura para cuando llegue el Lab 6.
> Todos los modelos Java en `core/model/` ya están alineados con esta estructura.

```
firestore/
│
├── usuarios/
│   └── {userId}/
│       ├── nombres, apellidos, tipoDoc, numDoc
│       ├── fechaNac, correo, telefono, domicilio
│       ├── fotoUrl, rol (RolUsuario), activo (boolean)
│       └── inmobiliariaId (null si es cliente)
│
├── inmobiliarias/
│   └── {inmobiliariaId}/
│       ├── nombre, adminId, ubicacion, correo, telefono
│       ├── logoUrl, bannerUrl
│       └── fotosUrl[] (mín. 2)
│
├── proyectos/
│   └── {proyectoId}/
│       ├── inmobiliariaId, nombre, descripcion
│       ├── ubicacion {lat, lng, direccion}
│       ├── estado (EN_PLANOS|EN_PREVENTA|EN_VENTA)
│       ├── precio, precioSeparacion
│       ├── tipologia[] [{metraje, numCuartos, precio}]
│       ├── areasComunes[] (strings)
│       ├── imagenesUrl[] (mín. 2)
│       ├── fechaEntregaEstimada
│       ├── codigoQR (URL generada)
│       └── asesoresIds[] (IDs de asesores asignados)
│
├── citas/
│   └── {citaId}/
│       ├── clienteId, asesorId, proyectoId
│       ├── fecha, hora
│       └── estado (PENDIENTE|CONFIRMADA|CANCELADA|EN_PROCESO)
│
├── separaciones/
│   └── {separacionId}/
│       ├── clienteId, asesorId, proyectoId
│       ├── monto, estado (POR_CONFIRMAR|APROBADA|NO_APROBADA|PAGADA)
│       ├── fechaCreacion, fechaLimite (10 min tras aprobación)
│       ├── valoracionCliente (1-5), observacionesCliente
│       └── pagoCompletado (boolean)
│
├── chats/
│   └── {chatId}/
│       ├── inmobiliariaId
│       ├── participantes[] [clienteId, asesorId]
│       ├── ultimoMensaje, ultimaFecha
│       └── mensajes/ (subcolección)
│           └── {mensajeId}/
│               ├── remitenteId, contenido, fecha, leido
│
├── notificaciones/
│   └── {userId}/
│       └── items/ (subcolección)
│           └── {notifId}/
│               ├── tipo, titulo, mensaje, leida, fecha
│
└── logs/                          (Solo lectura para superadmin)
    └── {logId}/
        ├── tipo, descripcion, usuarioId, fecha
```

---

## 7. Convenciones de nombres anti-conflicto

### Archivos Java
| Prefijo | Módulo | Ejemplo |
|---------|--------|---------|
| *(sin prefijo)* | `core/` | `Usuario.java`, `RoleRouter.java` |
| `Cliente` | `cliente/` | `ClienteHomeFragment.java` |
| `Asesor` | `asesor/` | `AsesorCitasViewModel.java` |
| `Admin` | `admin/` | `AdminProyectosFragment.java` |
| `Superadmin` | `superadmin/` | `SuperadminLogsFragment.java` |

### Layouts XML
| Prefijo | Uso |
|---------|-----|
| `activity_` | Activities (solo `activity_main.xml`) |
| `sistema_fragment_` | Fragments del módulo sistema |
| `cliente_fragment_` | Fragments del cliente |
| `asesor_fragment_` | Fragments del asesor |
| `admin_fragment_` | Fragments del admin |
| `superadmin_fragment_` | Fragments del superadmin |
| `item_sistema_` | Items RecyclerView de sistema |
| `item_cliente_` | Items RecyclerView del cliente |
| `item_asesor_` | Items RecyclerView del asesor |
| `item_admin_` | Items RecyclerView del admin |
| `item_superadmin_` | Items RecyclerView del superadmin |

### Strings XML
| Prefijo | Archivo | Ejemplo |
|---------|---------|---------|
| `app_` | `strings.xml` | `app_name` |
| `shared_` | `strings.xml` | `shared_btn_cancelar` |
| `sistema_` | `strings.xml` | `sistema_hint_correo` |
| `cliente_` | `strings_cliente.xml` | `cliente_title_home` |
| `asesor_` | `strings_asesor.xml` | `asesor_btn_registrar_sep` |
| `admin_` | `strings_admin.xml` | `admin_btn_aprobar` |
| `superadmin_` | `strings_superadmin.xml` | `superadmin_title_logs` |

### Drawables
| Prefijo | Uso |
|---------|-----|
| `shared_ic_` | Íconos usados por múltiples roles |
| `shared_bg_` | Backgrounds compartidos |
| `sistema_` | Recursos exclusivos del módulo sistema |
| `cliente_` | Recursos exclusivos del cliente |
| `asesor_` | Recursos exclusivos del asesor |
| `admin_` | Recursos exclusivos del admin |
| `superadmin_` | Recursos exclusivos del superadmin |

### IDs en layouts XML
```xml
<!-- Formato: {rol}_{tipo}_{nombre} -->
<!-- Ejemplos: -->
<TextView android:id="@+id/cliente_tv_precio" ... />
<Button   android:id="@+id/asesor_btn_registrar" ... />
<RecyclerView android:id="@+id/admin_rv_proyectos" ... />
<EditText android:id="@+id/sistema_et_correo" ... />
```

---

## 8. Flujo de trabajo Git sugerido

### Estructura de ramas
```
main              ← Rama estable. Solo merge cuando una feature está completa y probada.
└── develop       ← Rama de integración. Todos hacen merge aquí.
    ├── feature/sistema-auth       ← Integrante SISTEMA
    ├── feature/sistema-chat
    ├── feature/cliente-home       ← Integrante CLIENTE
    ├── feature/cliente-citas
    ├── feature/asesor-citas       ← Integrante ASESOR
    ├── feature/admin-proyectos    ← Integrante ADMIN
    └── feature/superadmin-users   ← Integrante SUPERADMIN
```

### Reglas para evitar conflictos
1. **Nunca editar archivos de otro módulo** sin avisar al responsable.
2. **`colors.xml` y `themes.xml`** — solo los modifica SISTEMA. Si alguien necesita un color, pide a SISTEMA que lo agregue.
3. **`strings.xml`** — cada uno edita solo su archivo (`strings_cliente.xml`, etc.).
4. **`nav_graph_root.xml`** — solo SISTEMA. Cada quien edita su propio navgraph.
5. **`AndroidManifest.xml`** — coordinar con SISTEMA antes de agregar permisos o activities.
6. **Modelos en `core/model/`** — coordinación entre todos antes de cambiar un campo.
7. Hacer **pull de `develop` antes de empezar a trabajar** cada sesión.
8. Los merges a `develop` se hacen por **Pull Request** con revisión de al menos un compañero.

### `.gitignore` recomendado (ya incluye Android Studio defaults)
```
*.iml
.gradle/
local.properties
.idea/
*.keystore
build/
captures/
```

---

## 9. Tabla de responsables por archivo

| Archivo / Paquete | Responsable | Pueden leer | Pueden modificar |
|---|---|---|---|
| `core/model/` | SISTEMA | TODOS | Solo SISTEMA (coordinado) |
| `core/repository/` | SISTEMA | TODOS | Solo SISTEMA |
| `core/util/` | SISTEMA | TODOS | Solo SISTEMA |
| `core/ui/auth/` | SISTEMA | — | Solo SISTEMA |
| `core/ui/chat/` | SISTEMA | — | Solo SISTEMA |
| `core/ui/notifications/` | SISTEMA | — | Solo SISTEMA |
| `core/ui/splash/` | SISTEMA | — | Solo SISTEMA |
| `core/ui/onboarding/` | SISTEMA | — | Solo SISTEMA |
| `cliente/` | CLIENTE | CLIENTE | Solo CLIENTE |
| `asesor/` | ASESOR | ASESOR | Solo ASESOR |
| `admin/` | ADMIN | ADMIN | Solo ADMIN |
| `superadmin/` | SUPERADMIN | SUPERADMIN | Solo SUPERADMIN |
| `res/layout/sistema_*` | SISTEMA | — | Solo SISTEMA |
| `res/layout/cliente_*` | CLIENTE | — | Solo CLIENTE |
| `res/layout/asesor_*` | ASESOR | — | Solo ASESOR |
| `res/layout/admin_*` | ADMIN | — | Solo ADMIN |
| `res/layout/superadmin_*` | SUPERADMIN | — | Solo SUPERADMIN |
| `res/navigation/nav_graph_root.xml` | SISTEMA | — | Solo SISTEMA |
| `res/navigation/nav_graph_sistema.xml` | SISTEMA | — | Solo SISTEMA |
| `res/navigation/nav_graph_cliente.xml` | CLIENTE | — | Solo CLIENTE |
| `res/navigation/nav_graph_asesor.xml` | ASESOR | — | Solo ASESOR |
| `res/navigation/nav_graph_admin.xml` | ADMIN | — | Solo ADMIN |
| `res/navigation/nav_graph_superadmin.xml` | SUPERADMIN | — | Solo SUPERADMIN |
| `res/values/colors.xml` | SISTEMA | TODOS | Solo SISTEMA |
| `res/values/themes.xml` | SISTEMA | TODOS | Solo SISTEMA |
| `res/values/strings.xml` | SISTEMA | TODOS | Solo SISTEMA |
| `res/values/strings_cliente.xml` | CLIENTE | — | Solo CLIENTE |
| `res/values/strings_asesor.xml` | ASESOR | — | Solo ASESOR |
| `res/values/strings_admin.xml` | ADMIN | — | Solo ADMIN |
| `res/values/strings_superadmin.xml` | SUPERADMIN | — | Solo SUPERADMIN |
| `AndroidManifest.xml` | SISTEMA | TODOS | Coordinado con SISTEMA |
| `build.gradle (Module :app)` | SISTEMA | TODOS | Coordinado con SISTEMA |
| `MainActivity.java` | SISTEMA | — | Solo SISTEMA |

---

*Documento generado para INMIA — 1TEL05 PUCP 2026-1*
