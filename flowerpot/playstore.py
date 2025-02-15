#     This file is part of Lawnchair Launcher.
#
#     Lawnchair Launcher is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     Lawnchair Launcher is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.

from playwright.sync_api import sync_playwright
from bs4 import BeautifulSoup
import re
from urllib.parse import urljoin

# Configuration
BASE_URL = "https://play.google.com"
CATEGORY_URL = f"{BASE_URL}/store/apps/category/"
TOP_URL = f"{BASE_URL}/store/apps/category/"
NEW_URL = f"{BASE_URL}/store/apps/new/category/"
DETAIL_URL = f"{BASE_URL}/store/apps/details?id="
CATEGORIES = [
    "PERSONALIZATION",
    "BOOKS_AND_REFERENCE",
    "SOCIAL",
    "COMMUNICATION",
    "TOOLS",
    "ENTERTAINMENT",
    "EDUCATION",
    "FINANCE",
    "BUSINESS",
    "LIFESTYLE",
    "MEDICAL",
    "MUSIC_AND_AUDIO",
    "PHOTOGRAPHY",
    "VIDEO_PLAYERS",
    "HEALTH_AND_FITNESS",
    "NEWS_AND_MAGAZINES",
    "BUSINESS",
    "FOOD_AND_DRINK",
    "MAPS_AND_NAVIGATION",
    "TRAVEL_AND_LOCAL",
    "SHOPPING",
]
ADDITIONAL_URLS = [
    # BASE_URL,
    # f"{BASE_URL}/store/apps/editorial",  This seems to disappear?
    # f"{BASE_URL}/store/apps/top",  Cannot find replacement
    f"{DETAIL_URL}app.lawnchair.play",
    f"{DETAIL_URL}app.lawnchair.lawnicons.play",
    f"{DETAIL_URL}ch.deletescape.lawnchair.plah",
    f"{DETAIL_URL}amirz.rootless.nexuslauncher",
    f"{DETAIL_URL}com.edzondm.linebit",
    f"{DETAIL_URL}com.jndapp.line.x.iconpack",
    f"{BASE_URL}/store/apps/dev?id=7714575631540799503",
]
PACKAGE_BLACKLIST = [
    r"\.cmcm\.",
    r"\.gomo\.",
    r"cheetah",
    r"sweetlovloc",
    r"hdwallpaper",
    r"ikeyboard",
    r"com\.gau",
    r"hdtheme",
    r"com\.amber\.",
    r"com\.soko",
    r"com\.andromo\.",  # App creator, not all apps with this are bad, but most of them are
    r"com\.jrj",
    r"live\.?wallpaper\.free",
    r"\.leafgreen\.",
    r"cleanmaster",
    r"emoji",
    r"cleaner\.booster",
    r"com\.toolapp",
    r"com\.jb\.",
    r"cootek",
    r"snowlife01",
    r"com\.visu",
    r"style_7",
    r"com\.triciaapps\.",
    r"bestfree",
    r"bestwall",
    r"bestliv",
    r"com\.motion",
    r"videodownloaderfor",
    r"lovesticker",
    r"com\.narvii\.amino\.x",
    r"kokowallpapers",
    r"girly",
    r"com\.jham\.",
    r"net\.pierrox\.lightning_launcher\.lp\.",
    r"ginlemon\.",
    r"s10",
    r"faceapp\.",
    r"facescan",
    r"lovetest",
    r"statussaver",
    r"battle\.?royale",
    r"webcreation\.",
    r"com\.wonderfulgames\.",
    r"com\.nexttechgamesstudio",
    r"com\.funpop\.",
    r"beautifulwall",
    r"com\.wpl\.",
    r"com\.blogspot\.euapps\.",
    r"com\.wsinc\.",
    r"cloudtv\.",
    r"\.cute\.",
    r"com\.american\.",
    r"com\.clear\.",
    r"\.cool\.",
    r"com\.free\.",
    r"com\.hd\.",
    r"amoledhd",
    r"com\.keyboard\.",
    r"com\.launcher\.wallpaper",
    r"com\.messenger\.sms\.",
    r"niceringtone",
    r"com\.pikasapps\.",
    r"com\.redraw\.",
    r"com\.ss\.",
    r"com\.thalia\.",
    r"com\.wallpaper",
    r"com\.warrior",
    r"glitter\.",
    r"hdwall",
    r"keyboard\.theme",
    r"lovequote",
    r"mobi\.infolife\.",
    r"\.horoscop",
    r"com\.bbg\.",
    r"channelpromoter",
    r"\.boost(er)?\.?cleaner",
    r"com\.ape\.",
    r"iphone",
    r"apus",
    r"boost(er)?\.?master",
    r"\.cooler\.",
    r"com\.booster\.",
    r"master\.booster",
    r"forinstagram",
    r"followers",
    r"galaxys",
    r"lionmobi",
    r"\.tohsoft\.",
    r"\.toolapp\.",
    r"com\.tool\.",
    r"free\.vpn",
    r"com\.vinwap\.",
    r"for\.whatsapp",
    r"forwhatsapp",
    r"forfacebook",
    r"frontdoor\.",
    r"free\.mp3",
    r"$theme",
    r"battery\.?save",
]


SCROLL_TIMES = 10
ID_MATCHER = r"\?id=(.*)"
CATEGORY_MATCHER = r"/category/(.*)"


def scrape_page(page, url):
    """Scrapes a single page for app IDs, handling scrolling and blacklisting."""
    print(f"Scraping: {url}")
    page.goto(url)
    page.wait_for_load_state("networkidle")  # Wait for initial content

    # Scroll to load more content (adjust as needed)
    for _ in range(SCROLL_TIMES):
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        page.wait_for_timeout(2000)  # Wait for 2 seconds after scrolling

    html_content = page.content()
    soup = BeautifulSoup(html_content, "html.parser")
    app_links = [
        a["href"]
        for a in soup.find_all("a", href=True)
        if "/store/apps/details?id=" in a["href"]
    ]
    app_ids = []

    for link in app_links:
        match = re.search(ID_MATCHER, link)
        if match:
            app_id = match.group(1)
            if len(app_id) < 45 and not any(
                re.search(pattern, app_id.lower()) for pattern in PACKAGE_BLACKLIST
            ):
                app_ids.append(app_id)
                print(f"Found app ID: {app_id}")
            else:
                print(f"Blacklisted or long ID: {app_id}")
    return list(dict.fromkeys(app_ids))  # Remove duplicates


def get_cluster_links(page, url):
    """Extracts cluster links from a given page."""
    print(f"Getting cluster links from: {url}")
    page.goto(url)
    page.wait_for_load_state("networkidle")
    html_content = page.content()
    soup = BeautifulSoup(html_content, "html.parser")
    # Handle relative and absolute URLs
    return list(
        dict.fromkeys([
            urljoin(BASE_URL, a["href"])
            for a in soup.find_all("a", href=True)
            if "/store/apps/collection/cluster" in a["href"]
        ])
    )


def main():
    category_to_apps = {}
    all_apps = []

    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        for category in CATEGORIES:
            category_to_apps[category] = []
            top_url = f"{TOP_URL}{category}"
            cluster_links = [top_url] + get_cluster_links(page, top_url)
            for cluster_url in cluster_links:
                app_ids = scrape_page(page, cluster_url)
                category_to_apps[category].extend(app_ids)
                category_to_apps[category] = list(
                    dict.fromkeys(category_to_apps[category])
                )  # Deduplicate
                all_apps.extend(app_ids)
                all_apps = list(dict.fromkeys(all_apps))

            with open(f"playstore/{category}", "w") as out:
                out.write("\n".join(category_to_apps[category]))
                out.write("\n")

        for url in ADDITIONAL_URLS:
            if "details?id=" in url:
                app_id = re.search(ID_MATCHER, url).group(1)
                if len(app_id) < 45 and not any(
                    re.search(pattern, app_id.lower()) for pattern in PACKAGE_BLACKLIST
                ):
                    page.goto(url)
                    page.wait_for_load_state("networkidle")
                    html_content = page.content()
                    soup = BeautifulSoup(html_content, "html.parser")
                    genre_element = soup.find(itemprop="genre")

                    if genre_element:
                        category_match = re.search(
                            CATEGORY_MATCHER, genre_element["href"]
                        )
                        if category_match:
                            category_name = category_match.group(1)
                            if category_name.startswith("GAME_"):
                                category_name = "GAME"

                            if category_name not in category_to_apps:
                                category_to_apps[category_name] = []
                            if app_id not in category_to_apps[category_name]:
                                category_to_apps[category_name].append(app_id)
                                with open(
                                    f"playstore/{category_name}", "a+"
                                ) as out_file:
                                    out_file.write(f"{app_id}\n")

            else:
                # I don't remember what this spaghetti supposed to do, but it work! :)
                cluster_links = get_cluster_links(page, url)
                for cluster_url in cluster_links:
                    app_ids = scrape_page(page, cluster_url)
                    for app_id in app_ids:
                        detail_page_url = f"{DETAIL_URL}{app_id}"
                        page.goto(detail_page_url)
                        page.wait_for_load_state("networkidle")
                        html_content = page.content()
                        soup = BeautifulSoup(html_content, "html.parser")
                        genre_element = soup.find(itemprop="genre")

                        if genre_element:
                            category_match = re.search(
                                CATEGORY_MATCHER, genre_element["href"]
                            )
                            if category_match:
                                category_name = category_match.group(1)
                                if category_name.startswith("GAME_"):
                                    category_name = "GAME"

                                if category_name not in category_to_apps:
                                    category_to_apps[category_name] = []
                                if app_id not in category_to_apps[category_name]:
                                    category_to_apps[category_name].append(app_id)
                                    with open(
                                        f"playstore/{category_name}", "a+"
                                    ) as out_file:
                                        out_file.write(f"{app_id}\n")
                        else:
                            print(f"No genre found for {app_id}")

        browser.close()

    print("Scraping complete.")


if __name__ == "__main__":
    main()
