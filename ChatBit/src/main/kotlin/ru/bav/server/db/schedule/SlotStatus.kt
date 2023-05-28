package ru.bav.server.db.schedule

enum class SlotStatus {
    AVAILABLE, WAIT_CONFIRM, BOOKED, NOTIFIED_DAY, NOTIFIED, RUNNING, CLOSED
}