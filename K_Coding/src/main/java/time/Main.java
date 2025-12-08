package time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        //Поточна дата: Виведіть на екран поточну дату.
        LocalDate today = LocalDate.now();
        System.out.println("Сьогодні: " + today); // Наприклад: 2025-09-16

        //Дата народження: Задайте свою дату народження (наприклад, 1990-05-15).
        LocalDate birthDate = LocalDate.of(1988, 4, 23);
        System.out.println("Дата народження: " + birthDate);

        //Кількість днів: Розрахуйте та виведіть на екран, скільки днів минуло з дати вашого народження до сьогодні.
        long daysBetween = ChronoUnit.DAYS.between(birthDate, today);
        System.out.println("Різниця в днях: " + daysBetween); // 366
        //2. Розрахунок термінів
        //Термін проєкту: Задайте поточну дату як дату початку проєкту.
        // Додайте до неї 3 місяці і 15 днів, щоб отримати дату завершення проєкту.
        //Виведіть дату початку та дату завершення у відформатованому вигляді,
        // наприклад: Початок: 16.09.2025, Завершення: 01.01.2026. Використайте DateTimeFormatter
        LocalDate startData = LocalDate.now();
        LocalDate endDate = today.plusDays(15).plusMonths(3);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedStartTime = startData.format(formatter);
        String formattedEndDateTime = endDate.format(formatter);
        System.out.println("Початок проекту: " + formattedStartTime);
        System.out.println("Кінець проекту: " + formattedEndDateTime);

        // 3. Робота з часом
        //Локальний час: Виведіть поточний час.
        LocalDateTime now = LocalDateTime.now();
        //Час зустрічі: Створіть об'єкт LocalDateTime для уявної зустрічі, наприклад, 18:00 сьогодні.
        LocalDateTime meeting = LocalDate.now().atTime(18, 0);
        //Виведіть повідомлення, якщо зустріч ще не відбулася, і повідомлення, якщо вона вже пройшла.
        if (now.isBefore(meeting)) {
            System.out.println("Зустріч ще не відбулася");
        } else
            System.out.println("Зустріч уже відбулася");
    }
}
