cmake_minimum_required(VERSION 3.4.1)

add_definitions(-DSQLITE_ENABLE_FTS5)

add_library(dictionary SHARED dictionary.c tokenizers.c sqlite3.c)

target_link_libraries(dictionary log)
