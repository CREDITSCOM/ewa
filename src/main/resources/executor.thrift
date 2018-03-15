namespace java executor
namespace cpp executor

struct ContractFile
{
    1:required string name
    2:required binary file
}

service ContractExecutor
{
    oneway void store(1:ContractFile file, 2:string address)

    oneway void execute(1:string address, 2:string method, 3:list<string> params)
}
