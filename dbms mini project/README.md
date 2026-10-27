# Student Record Manager

A simple Java + MySQL mini project with a browser interface built using HTML, CSS and JavaScript. It supports adding, listing, editing and deleting student records. Data lives in MySQL; the Java API uses JDBC prepared statements.

## Requirements

- JDK 17 or newer
- Maven 3.8+ (Apache Maven 3.10.0 is included under `.tools/`)
- MySQL Server 8+ and MySQL Workbench

## Database setup

1. Start MySQL Server and open `database/student_db.sql` in MySQL Workbench.
2. Execute the script. It creates `student_db` and the `students` table. The optional sample inserts are commented out.
3. Note your MySQL username and password.

## Run the app

From this project directory, set credentials in PowerShell (replace the example value). This setup uses the installed Temurin JDK 17 and the downloaded local Maven distribution:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
.\.tools\apache-maven-3.10.0\bin\mvn.cmd clean package
& "$env:JAVA_HOME\bin\java.exe" -jar target/student-record-manager-1.0.0-jar-with-dependencies.jar
```

Open <http://localhost:8080>. The app uses `DB_URL` if set; otherwise it connects to `jdbc:mysql://localhost:3306/student_db`. `PORT` can change the web port. Never commit real database credentials.

## Operations demonstrated

- **INSERT:** Add student form.
- **SELECT:** Directory list and refresh.
- **UPDATE:** Edit a row and save.
- **DELETE:** Delete a row after browser confirmation.
- **Validation:** Required fields, email format, text length and semester 1–12 are checked.

Use a unique email for each record. In Workbench, verify changes with `SELECT * FROM student_db.students;`.

## Project structure

```text
database/student_db.sql
src/main/java/edu/student/App.java
src/main/resources/web/index.html
src/main/resources/web/styles.css
src/main/resources/web/app.js
README.md
REPORT.md
```

## Screenshots for submission

Run the app and capture: (1) the application with a saved record, (2) the same record in Workbench, (3) an edited record reflected in both places, and (4) the directory after deletion. Place the images in `screenshots/` and embed them in the report. Screenshots are left for you to capture from your actual MySQL installation so they show real application/database state.
