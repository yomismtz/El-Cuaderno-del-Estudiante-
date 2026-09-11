# El Cuaderno del Estudiante 📓

Aplicación Android para estudiantes del ecosistema online formado por **ProfeCuaderno**, **El Cuaderno del Estudiante** y **El Escritorio del Director**.

## Descarga Android

Página pública del estudiante:

https://profecuaderno-api-production.up.railway.app/estudiante

Descarga directa del APK instalable más reciente:

https://github.com/yomismtz/El-Cuaderno-del-Estudiante-/releases/download/android-latest/El-Cuaderno-del-Estudiante.apk

El APK publicado por este flujo es una compilación de prueba para instalación directa. Android puede solicitar autorización para instalar aplicaciones desde el navegador o gestor de archivos.

## Estado actual

La app ya utiliza el backend central real. El estudiante puede:

- Registrarse e iniciar sesión como estudiante.
- Unirse a una clase mediante el código entregado por el docente.
- Consultar su institución y sus clases vinculadas.
- Ver avisos, pendientes, asistencia, calificaciones, horario, calendario y progreso.
- Mantener separados los estados **Sin evaluar** y una calificación real de `0`.
- Consultar actividades de equipo en las que participa.
- Enviar un reporte de participación únicamente sobre integrantes de su propio equipo mientras la actividad esté abierta.

Los datos académicos protegidos son de solo lectura en la app del estudiante: el alumno no modifica asistencia ni calificaciones.

## Privacidad de la coevaluación

El backend impide que un alumno se reporte a sí mismo o reporte a integrantes de otros equipos. El resumen que recibe el docente no expone la identidad del estudiante que realizó el reporte. El docente revisa y resuelve los reportes antes del cierre de la actividad.

## Relación con las otras apps

- **ProfeCuaderno** crea las clases, publica avisos y sincroniza la información académica.
- **El Cuaderno del Estudiante** muestra al alumno exclusivamente la información que le corresponde.
- **El Escritorio del Director** coordina la institución, docentes y horarios sin convertirse en una vista abierta del expediente académico de cada alumno.

Sitio público del ecosistema: https://profecuaderno-api-production.up.railway.app/

Política de privacidad conjunta: https://profecuaderno-api-production.up.railway.app/privacy

## Compilación

El workflow `.github/workflows/android.yml` ejecuta pruebas, genera APK/AAB de prueba y actualiza una descarga pública estable del APK cuando cambia `main`.

## Privacidad

La aplicación requiere autenticación para acceder a los datos online y el servidor aplica permisos por rol y pertenencia a clase/institución. Antes de un despliegue escolar definitivo deben documentarse el responsable legal del tratamiento, el contacto de privacidad, los plazos de conservación y el proveedor de infraestructura vigente. La política conjunta enlazada arriba describe el alcance actual del ecosistema online.
