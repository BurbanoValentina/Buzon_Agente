package com.ucc.complaints.dto.publiccomplaint;

import java.util.List;

public record FormOptionsResponse(List<String> categories, List<String> evidenceMimeTypes,
                                  int maxEvidenceFiles, long maxEvidenceBytes) { }
