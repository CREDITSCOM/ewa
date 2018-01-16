namespace java com.credits.thrift.gen

struct SharedStruct {
  1: i64 key
  2: string value
}

service SharedService {
  bool putPair(1: i64 key, 2: string value);
  void replacePair(1: i64 key, 2: string value);
  SharedStruct getStruct(1: i64 key)
}