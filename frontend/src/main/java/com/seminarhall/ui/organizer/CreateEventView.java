package com.seminarhall.ui.organizer;

import com.seminarhall.model.Event;
import com.seminarhall.model.EventCategory;
import com.seminarhall.service.EventService;
import com.seminarhall.ui.ThemeManager;
import com.seminarhall.ui.ThemeManager.Weight;
import com.seminarhall.ui.components.EventVisuals;
import com.seminarhall.ui.components.Icons;
import com.seminarhall.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * "Schedule New Campus Event" (web CreateEventPage): a "max-w-3xl" card with the web's fields
 * (Event Title, Category, Date, Auditorium / Hall, Seat Capacity) plus the ones the backend needs
 * that the web form leaves out (Start Time, End Time, Ticket Price, Description), in the same style.
 * Publishing calls eventService.createEvent as the logged-in organizer; the pencil in Manage Events
 * opens the same form to update an event.
 */
final class CreateEventView extends JPanel {

    interface Listener {
        void onCancel();

        void onSaved(Event saved, boolean updated);
    }

    /** The web's hall options, followed by the other halls that events use. */
    private static final List<String> WEB_HALLS = List.of("Seminar Hall A", "Seminar Hall B", "Main Auditorium");

    private static final DateTimeFormatter DATE_FIELD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FIELD = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final EventService eventService;
    private final Listener listener;

    private final JLabel heading;
    private final JLabel subtitle;
    private final ConsoleUi.FormField titleField = new ConsoleUi.FormField("e.g. Next-Gen Robotics Workshop");
    private final JComboBox<EventCategory> categoryCombo = ConsoleUi.select();
    private final ConsoleUi.FormField dateField = new ConsoleUi.FormField("yyyy-mm-dd");
    private final JComboBox<String> hallCombo = ConsoleUi.select();
    private final ConsoleUi.FormField capacityField = new ConsoleUi.FormField("100");
    private final ConsoleUi.FormField startField = new ConsoleUi.FormField("10:00 AM");
    private final ConsoleUi.FormField endField = new ConsoleUi.FormField("01:00 PM");
    private final ConsoleUi.FormField priceField = new ConsoleUi.FormField("0");
    private final ConsoleUi.FormArea descriptionArea =
            new ConsoleUi.FormArea("Hands-on practical session scheduled in the campus auditorium.", 3);
    private final ConsoleUi.Banner errorBanner = new ConsoleUi.Banner();
    private final StyledButton publishButton;
    private final JScrollPane scroll;

    /** Hall name → building (venue) as stored on existing events. */
    private final Map<String, String> buildingByHall = new LinkedHashMap<>();
    private Event editing;
    private boolean saving;

    CreateEventView(EventService eventService, Listener listener) {
        super(new BorderLayout());
        this.eventService = eventService;
        this.listener = listener;
        setOpaque(false);

        // "bg-[#10131d] border border-[#1d2232] rounded-3xl p-6 sm:p-10"
        ConsoleUi.Card card = new ConsoleUi.Card(ThemeManager.RADIUS_3XL, new ConsoleUi.Stack(0));
        card.setBorder(new EmptyBorder(41, 41, 41, 41));

        heading = ConsoleUi.text("Schedule New Campus Event", ConsoleUi.font(Weight.EXTRABOLD, 20), Color.WHITE, 28);
        subtitle = ConsoleUi.text("Allocate auditorium seating and publish passes to students.",
                ConsoleUi.font(Weight.REGULAR, 12), ThemeManager.TEXT_SECONDARY, 16);
        card.add(heading);
        card.add(Box.createVerticalStrut(4));    // mb-1
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));   // mb-6

        for (EventCategory category : EventVisuals.CHIP_ORDER) {
            if (category != EventCategory.ALL) {
                categoryCombo.addItem(category);
            }
        }
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof EventCategory) {
                    setText(categoryName((EventCategory) value));
                }
                return this;
            }
        });
        dateField.setTrailingIcon(Icons.get("calendar", 14, ThemeManager.TEXT_SECONDARY));
        startField.setTrailingIcon(Icons.get("clock", 14, ThemeManager.TEXT_SECONDARY));
        endField.setTrailingIcon(Icons.get("clock", 14, ThemeManager.TEXT_SECONDARY));
        setHallOptions(new ArrayList<>());

        // form: "space-y-4"
        JPanel form = new JPanel(new ConsoleUi.Stack(16));
        form.setOpaque(false);
        form.add(row(2, ConsoleUi.labeled("Event Title", titleField), ConsoleUi.labeled("Category", categoryCombo)));
        form.add(row(3, ConsoleUi.labeled("Date", dateField), ConsoleUi.labeled("Auditorium / Hall", hallCombo),
                ConsoleUi.labeled("Seat Capacity", capacityField)));
        form.add(row(3, ConsoleUi.labeled("Start Time", startField), ConsoleUi.labeled("End Time", endField),
                ConsoleUi.labeled("Ticket Price (₹)", priceField)));
        form.add(ConsoleUi.labeled("Description", descriptionArea));
        form.add(errorBanner);

        // "flex items-center justify-end gap-3 pt-4 border-t border-[#1d2232]"
        ConsoleUi.DarkButton cancel = new ConsoleUi.DarkButton("Cancel", 20);
        cancel.addActionListener(e -> {
            if (!saving) listener.onCancel();
        });
        publishButton = ConsoleUi.primaryButton("Publish Event →", 28);
        publishButton.addActionListener(e -> submit());
        ConsoleUi.GlowRow footer = new ConsoleUi.GlowRow(cancel, publishButton, true, 12);
        footer.setTopRule(16);
        form.add(footer);

        card.add(form);
        scroll = ConsoleUi.scrollPage(card, 768, 32, 32);
        add(scroll, BorderLayout.CENTER);
        startNew();
    }

    private static JPanel row(int columns, JComponent... cells) {
        double[] weights = new double[columns];
        java.util.Arrays.fill(weights, 1.0);
        JPanel row = new JPanel(new ConsoleUi.Columns(16, weights));
        row.setOpaque(false);
        for (JComponent cell : cells) {
            row.add(cell);
        }
        return row;
    }

    /** "Technical", "Workshop", "Cultural", "Seminar", "Hackathon", "Sports" (the web's option labels). */
    static String categoryName(EventCategory category) {
        String tag = EventVisuals.tagLabel(category);
        return tag.charAt(0) + tag.substring(1).toLowerCase(Locale.ENGLISH);
    }

    // ------------------------------------------------------------------ modes

    /** An empty form, with the web's defaults (100 seats, free entry). */
    void startNew() {
        editing = null;
        heading.setText("Schedule New Campus Event");
        subtitle.setText("Allocate auditorium seating and publish passes to students.");
        publishButton.setText("Publish Event →");
        titleField.setText("");
        categoryCombo.setSelectedIndex(0);
        dateField.setText(LocalDate.now().plusDays(7).format(DATE_FIELD));
        if (hallCombo.getItemCount() > 0) hallCombo.setSelectedIndex(0);
        capacityField.setText("100");
        startField.setText("10:00 AM");
        endField.setText("01:00 PM");
        priceField.setText("0");
        descriptionArea.setText("");
        setVenueEditable(true);
        errorBanner.clear();
        afterModeChange();
    }

    /** The same form filled with an event, to update it (hall and capacity are fixed once seats exist). */
    void startEdit(Event event) {
        editing = event;
        heading.setText("Update Campus Event");
        subtitle.setText("Change the schedule, pricing or details students see for this event.");
        publishButton.setText("Save Changes →");
        titleField.setText(event.getTitle());
        categoryCombo.setSelectedItem(event.getCategory());
        dateField.setText(event.getDate() != null ? event.getDate().format(DATE_FIELD) : "");
        if (event.getSeminarHall() != null && !hasHall(event.getSeminarHall())) {
            hallCombo.addItem(event.getSeminarHall());
        }
        hallCombo.setSelectedItem(event.getSeminarHall());
        capacityField.setText(String.valueOf(event.getTotalCapacity()));
        startField.setText(event.getStartTime() != null ? event.getStartTime().format(TIME_FIELD) : "");
        endField.setText(event.getEndTime() != null ? event.getEndTime().format(TIME_FIELD) : "");
        priceField.setText(formatPrice(event.getTicketPrice()));
        descriptionArea.setText(event.getDescription() != null ? event.getDescription() : "");
        setVenueEditable(false);
        errorBanner.clear();
        afterModeChange();
    }

    private void afterModeChange() {
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private void setVenueEditable(boolean editable) {
        hallCombo.setEnabled(editable);
        capacityField.setEnabled(editable);
        capacityField.setEditable(editable);
    }

    private static String formatPrice(double price) {
        return price == Math.rint(price) ? String.valueOf((long) price) : String.valueOf(price);
    }

    /** Hall choices: the web's three halls, then every other hall events use; remembers each hall's building. */
    void setKnownEvents(List<Event> events) {
        buildingByHall.clear();
        List<String> halls = new ArrayList<>();
        for (Event event : events) {
            String hall = event.getSeminarHall();
            if (hall != null && !hall.isBlank()) {
                halls.add(hall);
                if (event.getVenue() != null && !event.getVenue().isBlank()) {
                    buildingByHall.putIfAbsent(hall, event.getVenue());
                }
            }
        }
        setHallOptions(halls);
    }

    private void setHallOptions(List<String> eventHalls) {
        Object selected = hallCombo.getSelectedItem();
        Set<String> options = new LinkedHashSet<>(WEB_HALLS);
        eventHalls.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(options::add);
        hallCombo.removeAllItems();
        for (String hall : options) {
            hallCombo.addItem(hall);
        }
        if (selected != null) {
            if (!hasHall(selected.toString())) hallCombo.addItem(selected.toString());
            hallCombo.setSelectedItem(selected);
        }
    }

    private boolean hasHall(String hall) {
        for (int i = 0; i < hallCombo.getItemCount(); i++) {
            if (hallCombo.getItemAt(i).equals(hall)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ submit

    private void submit() {
        if (saving) return;
        errorBanner.clear();
        Event event;
        try {
            event = readForm();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        boolean update = editing != null;
        saving = true;
        publishButton.setEnabled(false);
        publishButton.setText(update ? "Saving…" : "Publishing…");
        new SwingWorker<Event, Void>() {
            @Override
            protected Event doInBackground() throws Exception {
                return update ? eventService.updateEvent(event) : eventService.createEvent(event);
            }

            @Override
            protected void done() {
                saving = false;
                publishButton.setEnabled(true);
                publishButton.setText(update ? "Save Changes →" : "Publish Event →");
                try {
                    Event saved = get();
                    listener.onSaved(saved, update);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void showError(String message) {
        errorBanner.error(message);
        revalidate();
        repaint();
    }

    /** Builds the event from the inputs; the service validates the rest and its message is shown as is. */
    private Event readForm() {
        Event event = new Event();
        if (editing != null) {
            event.setId(editing.getId());
            event.setVenue(editing.getVenue());
            event.setSeminarHall(editing.getSeminarHall());
            event.setTotalCapacity(editing.getTotalCapacity());
            event.setBookedSeats(editing.getBookedSeats());
        } else {
            String hall = hallCombo.getSelectedItem() != null ? hallCombo.getSelectedItem().toString() : "";
            event.setSeminarHall(hall);
            // the building is the one this hall already belongs to (the hall itself for a new hall)
            event.setVenue(buildingByHall.getOrDefault(hall, hall));
            event.setTotalCapacity(parseWhole(capacityField.getText(), "Seat capacity"));
        }
        event.setTitle(titleField.getText().trim());
        event.setCategory((EventCategory) categoryCombo.getSelectedItem());
        event.setDescription(descriptionArea.getText().trim());
        event.setDate(parseDate(dateField.getText()));
        event.setStartTime(parseTime(startField.getText(), "Start time"));
        event.setEndTime(parseTime(endField.getText(), "End time"));
        event.setTicketPrice(parsePrice(priceField.getText()));
        return event;
    }

    private static int parseWhole(String text, String what) {
        String value = text.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(what + " is required.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(what + " must be a whole number, e.g. 100.");
        }
    }

    private static double parsePrice(String text) {
        String value = text.trim().replace("₹", "").replace(",", "").trim();
        if (value.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ticket price must be a number, e.g. 0 for free entry or 150.");
        }
    }

    /** yyyy-mm-dd (as the web's date input), or dd-mm-yyyy / dd/mm/yyyy. */
    private static LocalDate parseDate(String text) {
        String value = text.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Event date is required.");
        }
        for (String pattern : new String[]{"yyyy-M-d", "d-M-yyyy", "d/M/yyyy"}) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        throw new IllegalArgumentException("Enter the date as yyyy-mm-dd, e.g. "
                + LocalDate.now().plusDays(7).format(DATE_FIELD) + ".");
    }

    /** 10:00 AM, 10 AM, 2:30pm or 14:30. */
    private static LocalTime parseTime(String text, String what) {
        String value = text.trim().toUpperCase(Locale.ENGLISH).replaceAll("\\s+", " ")
                .replaceAll("(\\d)(AM|PM)$", "$1 $2");
        if (value.isEmpty()) {
            throw new IllegalArgumentException(what + " is required.");
        }
        for (String pattern : new String[]{"h:mm a", "h a", "H:mm"}) {
            try {
                DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive()
                        .appendPattern(pattern).toFormatter(Locale.ENGLISH);
                return LocalTime.parse(value, format);
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        throw new IllegalArgumentException(what + " must look like 10:00 AM or 14:30.");
    }
}
