package org.mangorage.mangobot.modules.logs.modules;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LastStack extends StackTraceReader {

	
	public String[] package_denylist = {
			"java.",
			"jdk.",
			"sun.",
			"com.sun."			
	};
	
	
    @Override
    public void analyse(String log, StringBuilder message) {
    	message.append("Analysing last").append(nl);
        List<String> traces = getTraces(log);
        if (traces.isEmpty()) {
            return;
        }

        String lastTrace = traces.get(traces.size() - 1);

        List<String> filteredLines = Stream.of(lastTrace.split(nl))
            .filter(line -> !isLineDenylisted(line.strip()))
            .collect(Collectors.toList());

        for (String line : filteredLines) {
           message.append(line).append(nl);
        }
    }

    private boolean isLineDenylisted(String line) {
        if (line.startsWith("at")) {
            String packageCandidate = extractPackageCandidate(line);

            for (String packagePrefix : package_denylist) {
                if (packageCandidate.startsWith(packagePrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String extractPackageCandidate(String line) {
        int startIdx = line.indexOf(' ') + 1; 
        int firstDotIdx = line.indexOf('.', startIdx); 

        if (firstDotIdx == -1) {
            return ""; 
        }

        int endIdx = line.indexOf(' ', firstDotIdx);
        if (endIdx == -1) {
            endIdx = line.indexOf('(', firstDotIdx); 
        }
        if (endIdx == -1) {
            endIdx = line.length(); 
        }

        String candidate = line.substring(startIdx, endIdx).trim();

        int lastSlashIdx = candidate.lastIndexOf('/');
        if (lastSlashIdx != -1) {
            candidate = candidate.substring(lastSlashIdx + 1); 
        }

        int lastDotIdx = candidate.lastIndexOf('.');
        if (lastDotIdx == -1) {
            return ""; 
        }

        return candidate.substring(0, lastDotIdx);
    }
}
