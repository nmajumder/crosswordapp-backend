/* TABLE: USERS
 * 	stores user info (token, email, username, password, crossword settings)
 *	no foreign key dependencies
 * 	is referenced from all other tables that rely on user info
 */
CREATE TABLE USERS (token varchar(64) primary key, email varchar(32) not null, username varchar(32) not null, password varchar(64) not null, color_scheme integer, inactivity_timer integer);
CREATE INDEX users_token_idk ON USERS (token);

/* TABLE: BOARDS
 * 	stores current status info for full sized crosswords linked to user id (crossword_id, grid status, selected square info, timer value, completed bool)
 *	foreign key dependency on USERS
 */
CREATE TABLE BOARDS (user_id varchar(64) REFERENCES users(token), crossword_id varchar(64), grid json not null, selection json not null, seconds integer not null, completed boolean not null, difficulty_rating integer, enjoyment_rating integer,
PRIMARY KEY (user_id, crossword_id));
CREATE INDEX boards_user_idx ON BOARDS (user_id);
CREATE INDEX boards_crossword_idx ON BOARDS (crossword_id);

/* TABLE: CATEGORY_STATS
 * 	stores all time stats for minis per user id in each category
 *		row per user per size per difficulty of mini grids
 *		cols for:
 *			aggregates (ints): completed, started, revealed, checked
 *			best finish time and date of that occurrence, as well as the rolling average finish time
 *			a map of every date of a completed puzzle to the number of completions that day
 *	foreign key dependency on USERS
 */
CREATE TABLE CATEGORY_STATS (user_id varchar(64) REFERENCES users(token), size integer, difficulty varchar(32), completed integer, started integer, best_time integer, best_date date, average_time float, checked integer, revealed integer, activity_map json);
CREATE INDEX cat_stats_usr_idx ON CATEGORY_STATS (user_id);
CREATE INDEX cat_stats_size_idx ON CATEGORY_STATS (size);
CREATE INDEX cat_stats_diff_idx ON CATEGORY_STATS (difficulty);
CREATE INDEX cat_stats_time_idx ON CATEGORY_STATS (best_time);

/* TABLE: TOTAL_STATS
 * 	stores all time total stats for minis per user id
 *		row per user
 *		cols for:
 *			aggregates (ints): completed, started, revealed
 *			current rolling streak (and the last completed puzzle date for day-of calculations), and longest streak ever
 *	foreign key dependency on USERS
 */
CREATE TABLE TOTAL_STATS (user_id varchar(64) REFERENCES users(token), completed integer, started integer, revealed integer, current_streak integer, last_completed date, longest_streak integer);
CREATE INDEX tot_stats_usr_idx ON TOTAL_STATS (user_id);
CREATE INDEX tot_stats_completed_idx ON TOTAL_STATS (completed);
CREATE INDEX tot_stats_started_idx ON TOTAL_STATS (started);
CREATE INDEX tot_stats_revealed_idx ON TOTAL_STATS (revealed);
CREATE INDEX tot_stats_curstreak_idx ON TOTAL_STATS (current_streak);
CREATE INDEX tot_stats_longstreak_idx ON TOTAL_STATS (longest_streak);

/* TABLE: MINIBOARDS
 * 	stores current generated mini per user (a json grid, size, difficulty)
 * 	can only be 1 row per user at any given time, will overwrite when a new one is generated
 *	foreign key dependency on USERS
 */
CREATE TABLE MINIBOARDS (user_id varchar(64) REFERENCES users(token), mini_solution json not null, size integer not null, difficulty varchar(16) not null, checked boolean not null, revealed boolean not null);
CREATE INDEX miniboards_user_index ON MINIBOARDS (user_id);