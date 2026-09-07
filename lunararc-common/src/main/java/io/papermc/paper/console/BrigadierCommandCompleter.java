package io.papermc.paper.console;

import java.util.List;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import net.minecraft.server.dedicated.DedicatedServer;

public class BrigadierCommandCompleter {
    private final DedicatedServer server;

    public BrigadierCommandCompleter(DedicatedServer server) { this.server = server; }

    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates, List<Completion> completions) {
        java.util.Set<String> values = new java.util.HashSet<>();
        if (server.overworld() != null && io.papermc.paper.configuration.GlobalConfiguration.get().console.enableBrigadierCompletions) {
            var dispatcher = server.getCommands().getDispatcher();
            var parsed = dispatcher.parse(line.line(), server.createCommandSourceStack());
            var suggestions = dispatcher.getCompletionSuggestions(parsed, line.cursor()).join();
            int wordStart = line.cursor() - line.wordCursor();
            for (var suggestion : suggestions.getList()) {
                String applied = suggestion.apply(line.line());
                int end = suggestion.getRange().getStart() + suggestion.getText().length();
                String value = applied.substring(Math.min(wordStart, suggestion.getRange().getStart()), end);
                String tooltip = suggestion.getTooltip() == null ? null : suggestion.getTooltip().getString();
                candidates.add(new Candidate(value, value, null, tooltip, null, null, false));
                values.add(suggestion.getText());
            }
        }
        for (Completion completion : completions) {
            String value = completion.suggestion();
            if (value.isEmpty() || !values.add(value)) continue;
            String tooltip = completion.tooltip() == null ? null
                    : net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(completion.tooltip());
            candidates.add(new Candidate(value, value, null, tooltip, null, null, false));
        }
    }
}
