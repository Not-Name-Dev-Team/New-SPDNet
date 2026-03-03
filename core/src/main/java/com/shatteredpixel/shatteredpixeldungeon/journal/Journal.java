/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Sender;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CCatalogUpdate;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CBestiaryUpdate;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CDocumentUpdate;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.List;

public class Journal {
	
	public static final String JOURNAL_FILE = "journal.dat";
	
	private static boolean loaded = false;
	
	public static void loadGlobal(){
	}

	// SPDNet: 从云端加载 Journal 数据
	public static void loadFromCloud(List<com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJournals.CatalogData> catalogs,
	                                List<com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJournals.BestiaryData> bestiaries,
	                                List<com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJournals.DocumentData> documents) {

		// 加载 Catalog 数据
		if (catalogs != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJournals.CatalogData data : catalogs) {
				try {
					Catalog cat = Catalog.valueOf(data.getCatalogType());
					Class<?> itemClass = Class.forName(data.getItemClass());
					if (cat.seen.containsKey(itemClass)) {
						cat.seen.put(itemClass, data.isSeen());
						cat.useCount.put(itemClass, data.getUseCount());
					}
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}
			}
		}

		// 加载 Bestiary 数据
		if (bestiaries != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJournals.BestiaryData data : bestiaries) {
				try {
					Bestiary cat = Bestiary.valueOf(data.getBestiaryType());
					Class<?> entityClass = Class.forName(data.getEntityClass());
					if (cat.seen.containsKey(entityClass)) {
						cat.seen.put(entityClass, data.isSeen());
						cat.encounterCount.put(entityClass, data.getEncountered());
					}
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}
			}
		}

		// 加载 Document 数据
		if (documents != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJournals.DocumentData data : documents) {
				try {
					Document doc = Document.valueOf(data.getDocumentType());
					if (doc.pagesStates.containsKey(data.getPageName())) {
						doc.pagesStates.put(data.getPageName(), data.getState());
					}
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException(e);
				}
			}
		}
	}

	//package-private
	static boolean saveNeeded = false;


	// SPDNet: 发送 Catalog 更新到服务器
	public static void sendCatalogUpdate(String catalogType, String itemClass, boolean seen, int useCount) {
		try {
			Sender.sendCatalogUpdate(new CCatalogUpdate(catalogType, itemClass, seen, useCount));
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	// SPDNet: 发送 Bestiary 更新到服务器
	public static void sendBestiaryUpdate(String bestiaryType, String entityClass, boolean seen, int encountered) {
		try {
			Sender.sendBestiaryUpdate(new CBestiaryUpdate(bestiaryType, entityClass, seen, encountered));
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	// SPDNet: 发送 Document 更新到服务器
	public static void sendDocumentUpdate(String documentType, String pageName, int state) {
		try {
			Sender.sendDocumentUpdate(new CDocumentUpdate(documentType, pageName, state));
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	public static void saveGlobal(){
		saveGlobal(false);
	}

	public static void saveGlobal(boolean force){
	}

}
