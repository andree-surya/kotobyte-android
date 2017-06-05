cmake_minimum_required(VERSION 3.4.1)

add_library(search_database SHARED search_database.c)

target_link_libraries(search_database log asset_manager_jni)
