/****************************************************
 * Moses - factored phrase-based language decoder   *
 * Copyright (C) 2015 University of Edinburgh       *
 * Licensed under GNU LGPL Version 2.1, see COPYING *
 ****************************************************/

#ifndef FILESYSTEM_H
#define FILESYSTEM_H

#include <boost/filesystem.hpp>

namespace sto {

void remove_all(const std::string &p) {
  boost::system::error_code ec;
  boost::filesystem::remove_all(p, ec);
}

void create_directory(const std::string &p) {
  boost::filesystem::create_directory(p);
}

void copy_file(const std::string &from, const std::string &to) {
  boost::filesystem::copy_file(from, to, boost::filesystem::copy_option::overwrite_if_exists);
}

} // namespace sto

#endif //FILESYSTEM_H
