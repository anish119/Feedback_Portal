# Feedback Portal

A small web app where users log in and submit feedback, and an admin can see, filter, edit and delete everything that comes in. Built with Spring Boot, Thymeleaf and PostgreSQL.


## What it does

**Users**
- Register with a user ID and password. You're logged in straight away, no second login.
- Submit new feedback.
- See your own past feedback and edit it. You can't see or edit anyone else's.

**Admin**
- One page showing feedback from all users.
- Filter by user ID and by date. The user ID box is a dropdown you can also type into, and the list narrows as you type.
- Edit or delete any feedback. Editing changes only the text, so the original user and date stay as they were.

Anyone who hits a page they aren't allowed to see (a user opening an admin URL, an admin opening a user page, or someone poking at another user's edit link) gets logged out and sent back to the login page.

## Running it locally

You need Java 17+, Maven and PostgreSQL.

1. Create the database:

```sql
   CREATE DATABASE feedback_portal;
```

2. Copy `.env.example` to `.env` and fill it in:

```properties
   DB_URL=jdbc:postgresql://localhost:5432/feedback_portal
   DB_USERNAME=postgres
   DB_PASSWORD=your_password

   ADMIN_USER_ID=your_admin_id
   ADMIN_PASSWORD=pick_something_strong
```

3. Start it from the project root (the `.env` path is relative, so running from another folder won't work):

```bash
   mvn spring-boot:run
```

4. Open http://localhost:8080

Tables are created by Hibernate on first run. You don't need to write any SQL for them.

## The admin account

There's no admin signup. The admin is created at startup from `ADMIN_USER_ID` and `ADMIN_PASSWORD` in `.env`, and the password is stored hashed. If that user already exists in the database, the seeder leaves it alone, so changing the password in `.env` later won't update an existing admin. Delete the row (or change it in the DB) if you need to reset it.

Registration refuses the admin's user ID, and every new account gets the USER role. There's no way to pick a role from the form.

## Database

| users | |
|---|---|
| user_id | primary key |
| password | BCrypt hash |
| role | `USER` or `ADMIN` |

| feedbacks | |
|---|---|
| feedback_id | primary key, auto-generated |
| user_id | foreign key to users |
| feedback | text |
| date | set when the feedback is created |

## Routes

| Route | Who | What |
|---|---|---|
| `/login`, `/register` | anyone | auth pages |
| `/feedback/new` | user | submit feedback |
| `/feedback/my` | user | own feedback, with edit |
| `/admin/feedbacks` | admin | everything, with filters, edit and delete |

Ownership is checked on the server for every user edit, so changing the ID in the URL doesn't get you into someone else's feedback. Delete is POST-only and CSRF protection is on for all forms.


## Stack

Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Thymeleaf, PostgreSQL, Maven.