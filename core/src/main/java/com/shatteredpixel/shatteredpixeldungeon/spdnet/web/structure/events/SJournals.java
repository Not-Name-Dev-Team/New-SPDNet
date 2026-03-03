package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SJournals extends Data {
	private List<CatalogData> catalogs;
	private List<BestiaryData> bestiaries;
	private List<DocumentData> documents;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CatalogData {
		private String catalogType;
		private String itemClass;
		private boolean seen;
		private int useCount;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BestiaryData {
		private String bestiaryType;
		private String entityClass;
		private boolean seen;
		private int encountered;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DocumentData {
		private String documentType;
		private String pageName;
		private int state;
	}
}
