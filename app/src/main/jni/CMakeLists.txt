cmake_minimum_required(VERSION 3.4.1)

add_library(dictionary SHARED dictionary.c sqlite3.c)
add_definitions(-DSQLITE_ENABLE_FTS5)

target_link_libraries(dictionary log)
