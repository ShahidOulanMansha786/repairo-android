package com.carrepair.app.domain.utils

fun validateSignupFields(
    fullName: String,
    email: String,
    phone: String
): Triple<String?, String?, String?> {

    // Validate full name
    val nameError = when {
        fullName.isBlank() -> "Full name is required"
        fullName.trim().length < 2 -> "Name must be at least 2 characters"

        // Regex: only letters (a-z, A-Z) and spaces allowed
        // ^[a-zA-Z ]+$ means: from start to end, only these characters
        !fullName.trim().matches(Regex("^[a-zA-Z ]+$")) -> "Name must contain only letters"
        else -> null // null means no error
    }

    // Validate email
    val emailError = when {
        email.isBlank() -> "Email is required"

        // android.util.Patterns.EMAIL_ADDRESS is a built-in Android regex
        // It covers standard email formats like user@example.com
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
            "Enter a valid email address"

        else -> null
    }

    // Validate phone
    val phoneError = when {
        phone.isBlank() -> "Phone number is required"

        // all { it.isDigit() } checks every character is a digit
        !phone.trim().all { it.isDigit() } -> "Phone must contain only numbers"

        phone.trim().length < 11 -> "Phone must be at least 11 digits"
        phone.trim().length > 11 -> "Phone must be at most 11 digits"
        else -> null
    }

    // Triple carries three values at once — one per field
    return Triple(nameError, emailError, phoneError)
}