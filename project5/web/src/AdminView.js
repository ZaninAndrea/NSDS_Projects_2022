import "./App.css"
import React, { useState, useEffect } from "react"
import {
    Stack,
    Button,
    Card,
    Grid,
    Badge,
    UnstyledButton,
    Group,
    Text,
    Flex,
    CloseButton,
    TextInput,
} from "@mantine/core"
import { IconTrash, IconPlus } from "@tabler/icons"

function MainLink({ label, selected, onClick }) {
    return (
        <UnstyledButton
            sx={(theme) => ({
                display: "block",
                width: "100%",
                padding: theme.spacing.xs,
                borderRadius: theme.radius.sm,
                marginBottom: "8px",
                color: theme.black,
                backgroundColor: selected
                    ? theme.colors.green[1]
                    : theme.colors.yellow[1],
                "&:hover": {
                    backgroundColor: selected
                        ? theme.colors.green[2]
                        : theme.colors.yellow[2],
                },
            })}
            onClick={onClick}
        >
            <Group>
                <Text size="sm">{label}</Text>
            </Group>
        </UnstyledButton>
    )
}

class AdminView extends React.Component {
    state = {
        items: [],
        newItem: "",
    }

    componentDidMount() {
        this.getItems()
        this.fetchInterval = setInterval(this.getItems, 1000)
    }

    componentWillUnmount() {
        clearInterval(this.fetchInterval)
    }

    getItems = async () => {
        const res = await fetch("http://localhost:8003/items").then((res) =>
            res.json()
        )
        this.setState({
            items: res.map((i) => Object.values(i)[0]),
        })
    }

    render() {
        return (
            <>
                <Card
                    style={{
                        maxWidth: "450px",
                        margin: "0 auto 32px auto",
                    }}
                    className="admin-input-box"
                >
                    <TextInput
                        style={{ width: "100%" }}
                        value={this.state.newItem}
                        onChange={(e) => {
                            this.setState({
                                newItem: e.currentTarget.value,
                            })
                        }}
                    ></TextInput>
                    <Button
                        variant="light"
                        style={{
                            marginLeft: "8px",
                        }}
                        color="green"
                        disabled={!this.state.newItem}
                        onClick={() => {
                            fetch("http://localhost:8003/items", {
                                method: "POST",
                                headers: {
                                    "Content-Type": "text/plain",
                                },
                                body: this.state.newItem + "\ntrue",
                            })
                            this.setState({ newItem: "" })
                        }}
                    >
                        <IconPlus />
                    </Button>
                </Card>
                <Stack
                    style={{
                        maxWidth: "300px",
                        margin: "auto",
                    }}
                >
                    {this.state.items.map((item) => (
                        <Flex key={item.name}>
                            <MainLink
                                label={item.name}
                                selected={item.available}
                                onClick={() => {
                                    fetch("http://localhost:8003/items", {
                                        method: "POST",
                                        headers: {
                                            "Content-Type": "text/plain",
                                        },
                                        body:
                                            item.name + "\n" + !item.available,
                                    })
                                }}
                            />
                            <Button
                                variant="subtle"
                                style={{ marginLeft: "8px", height: "42px" }}
                                color="red"
                                onClick={() => {
                                    fetch("http://localhost:8003/items", {
                                        method: "DELETE",
                                        headers: {
                                            "Content-Type": "text/plain",
                                        },
                                        body: item.name,
                                    })
                                }}
                            >
                                <IconTrash />
                            </Button>
                        </Flex>
                    ))}
                </Stack>
            </>
        )
    }
}

export default AdminView
