namespace java com.credits.thrift
namespace cpp executor

struct APIResponse
{
    1: i8 code
    2: string message
	3: binary contractState
}

service ContractExecutor
{
	APIResponse executeByteCode(1:string address, 2:binary byteCode, 3:binary contractState, 4:string method, 5:list<string> params)
}
