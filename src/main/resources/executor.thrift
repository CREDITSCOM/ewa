namespace java executor
namespace cpp executor

struct ContractFile
{
    1:required string name
    2:required binary file
}

struct APIResponse
{
    1: i8 code
    2: string message
}

service ContractExecutor
{
    APIResponse store(1:ContractFile file, 2:string address)

    APIResponse execute(1:string address, 2:string method, 3:list<string> params)
}
