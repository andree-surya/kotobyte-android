cmake_minimum_required(VERSION 3.4.1)

add_library(dictionary_database SHARED dictionary_database.c)

target_link_libraries(dictionary_database log)
